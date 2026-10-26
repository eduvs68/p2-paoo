package br.fatec.simaqinpe.service;

import br.fatec.simaqinpe.repository.FocoFogoImportacaoRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportacaoService {

    private static final int TAMANHO_LOTE = 1000;
    private static final String SEPARADOR = ",";
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> COLUNAS_OBRIGATORIAS = List.of(
            "latitude", "longitude", "data_pas", "satelite", "estado", "municipio", "bioma", "risco_fogo");

    private final FocoFogoImportacaoRepository repository;

    public ImportacaoService(FocoFogoImportacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ResumoImportacao importar(MultipartFile arquivo) {
        validarArquivo(arquivo);
        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {
            Cabecalho cabecalho = lerCabecalho(leitor);
            String primeiraLinha = proximaLinha(leitor);
            if (primeiraLinha == null) {
                throw new RegistroInvalidoException("O arquivo não possui registros além do cabeçalho");
            }
            repository.limparTabela();
            return processar(leitor, primeiraLinha, cabecalho);
        } catch (IOException e) {
            throw new RegistroInvalidoException("Não foi possível ler o arquivo enviado");
        }
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegistroInvalidoException("O arquivo está vazio");
        }
        String nome = arquivo.getOriginalFilename();
        if (nome == null || !nome.toLowerCase().endsWith(".csv")) {
            throw new RegistroInvalidoException("O arquivo deve ter a extensão .csv");
        }
    }

    private Cabecalho lerCabecalho(BufferedReader leitor) throws IOException {
        String linha = proximaLinha(leitor);
        if (linha == null) {
            throw new RegistroInvalidoException("O arquivo não possui cabeçalho");
        }
        if (linha.startsWith("﻿")) {
            linha = linha.substring(1);
        }
        String[] nomes = linha.split(SEPARADOR, -1);
        Map<String, Integer> posicoes = new HashMap<>();
        for (int i = 0; i < nomes.length; i++) {
            posicoes.put(nomes[i].trim().toLowerCase(), i);
        }
        List<String> ausentes = COLUNAS_OBRIGATORIAS.stream()
                .filter(coluna -> !posicoes.containsKey(coluna))
                .toList();
        if (!ausentes.isEmpty()) {
            throw new RegistroInvalidoException("Estrutura do arquivo inválida: colunas ausentes " + ausentes
                    + ". Verifique o cabeçalho e se o separador é a vírgula");
        }
        return new Cabecalho(posicoes, nomes.length);
    }

    private ResumoImportacao processar(BufferedReader leitor, String primeiraLinha, Cabecalho cabecalho)
            throws IOException {
        long lidas = 0;
        long importados = 0;
        long rejeitados = 0;
        long duplicados = 0;
        Map<String, Long> motivos = new LinkedHashMap<>();
        List<FocoLinha> lote = new ArrayList<>(TAMANHO_LOTE);

        for (String linha = primeiraLinha; linha != null; linha = proximaLinha(leitor)) {
            lidas++;
            try {
                lote.add(converter(linha, cabecalho));
            } catch (RegistroInvalidoException e) {
                rejeitados++;
                motivos.merge(e.getMessage(), 1L, Long::sum);
            }
            if (lote.size() == TAMANHO_LOTE) {
                int inseridos = repository.inserirLote(lote);
                importados += inseridos;
                duplicados += lote.size() - inseridos;
                lote.clear();
            }
        }
        if (!lote.isEmpty()) {
            int inseridos = repository.inserirLote(lote);
            importados += inseridos;
            duplicados += lote.size() - inseridos;
        }

        String mensagem = String.format(
                "Importação concluída. Linhas lidas: %d | importadas: %d | rejeitadas: %d | duplicadas: %d",
                lidas, importados, rejeitados, duplicados);
        return new ResumoImportacao(lidas, importados, rejeitados, duplicados, motivos, mensagem);
    }

    private FocoLinha converter(String linha, Cabecalho cabecalho) {
        String[] campos = linha.split(SEPARADOR, -1);
        if (campos.length != cabecalho.total()) {
            throw new RegistroInvalidoException("Quantidade de colunas diferente do cabeçalho");
        }

        LocalDateTime dataHora = lerDataHora(cabecalho.valor(campos, "data_pas"));
        String satelite = lerTexto(cabecalho.valor(campos, "satelite"), "Satélite", 30).toUpperCase();
        String uf = Estados.paraSigla(cabecalho.valor(campos, "estado"));
        if (uf == null) {
            throw new RegistroInvalidoException("Estado vazio ou desconhecido");
        }
        String municipio = lerTexto(cabecalho.valor(campos, "municipio"), "Município", 100).toUpperCase();
        String bioma = lerTexto(cabecalho.valor(campos, "bioma"), "Bioma", 30);
        BigDecimal latitude = lerCoordenada(cabecalho.valor(campos, "latitude"), "Latitude", 90);
        BigDecimal longitude = lerCoordenada(cabecalho.valor(campos, "longitude"), "Longitude", 180);
        BigDecimal riscoFogo = lerRiscoFogo(cabecalho.valor(campos, "risco_fogo"));

        return new FocoLinha(dataHora, satelite, uf, municipio, bioma, latitude, longitude, riscoFogo);
    }

    private LocalDateTime lerDataHora(String texto) {
        try {
            return LocalDateTime.parse(texto, FORMATO_DATA_HORA);
        } catch (DateTimeParseException e) {
            throw new RegistroInvalidoException("Data e hora vazias ou em formato inválido");
        }
    }

    private String lerTexto(String texto, String campo, int tamanhoMaximo) {
        if (texto.isEmpty()) {
            throw new RegistroInvalidoException(campo + " vazio");
        }
        if (texto.length() > tamanhoMaximo) {
            throw new RegistroInvalidoException(campo + " com mais de " + tamanhoMaximo + " caracteres");
        }
        return texto;
    }

    private BigDecimal lerCoordenada(String texto, String campo, int limite) {
        BigDecimal valor;
        try {
            valor = new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new RegistroInvalidoException(campo + " vazia ou inválida");
        }
        if (valor.abs().compareTo(BigDecimal.valueOf(limite)) > 0) {
            throw new RegistroInvalidoException(campo + " fora do intervalo permitido");
        }
        return valor;
    }

    private BigDecimal lerRiscoFogo(String texto) {
        try {
            BigDecimal valor = new BigDecimal(texto);
            boolean valido = valor.signum() >= 0 && valor.compareTo(BigDecimal.ONE) <= 0;
            return valido ? valor : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String proximaLinha(BufferedReader leitor) throws IOException {
        String linha = leitor.readLine();
        while (linha != null && linha.isBlank()) {
            linha = leitor.readLine();
        }
        return linha;
    }

    private record Cabecalho(Map<String, Integer> posicoes, int total) {

        String valor(String[] campos, String coluna) {
            return campos[posicoes.get(coluna)].trim();
        }
    }
}
