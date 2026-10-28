package br.fatec.simaqinpe.service;

import br.fatec.simaqinpe.model.FocoFogo;
import br.fatec.simaqinpe.repository.FocoFogoEstatisticasRepository;
import br.fatec.simaqinpe.repository.FocoFogoRepository;
import br.fatec.simaqinpe.repository.FocoFogoSpecs;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class FocoFogoService {

    private static final String MSG_DUPLICADO = "Já existe um foco com a mesma data, satélite e coordenadas";

    private final FocoFogoRepository repository;
    private final FocoFogoEstatisticasRepository estatisticasRepository;

    public FocoFogoService(FocoFogoRepository repository, FocoFogoEstatisticasRepository estatisticasRepository) {
        this.repository = repository;
        this.estatisticasRepository = estatisticasRepository;
    }

    public Page<FocoFogo> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<FocoFogo> filtrar(String uf, String bioma, String satelite,
                                  LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        return repository.findAll(montarFiltro(uf, bioma, satelite, dataInicio, dataFim), pageable);
    }

    public List<ContagemMensal> contarPorMes(String uf, String bioma, String satelite,
                                             LocalDate dataInicio, LocalDate dataFim) {
        return estatisticasRepository.contarPorMes(montarFiltro(uf, bioma, satelite, dataInicio, dataFim));
    }

    public OpcoesFiltro listarOpcoes() {
        return new OpcoesFiltro(repository.listarBiomas(), repository.listarSatelites());
    }

    private Specification<FocoFogo> montarFiltro(String uf, String bioma, String satelite,
                                                 LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new RegistroInvalidoException("A data inicial não pode ser posterior à data final");
        }
        Specification<FocoFogo> filtro = Specification.unrestricted();
        if (preenchido(uf)) {
            String sigla = uf.trim().toUpperCase();
            if (!Estados.siglaValida(sigla)) {
                throw new RegistroInvalidoException("UF inválida: " + sigla);
            }
            filtro = filtro.and(FocoFogoSpecs.uf(sigla));
        }
        if (preenchido(bioma)) {
            filtro = filtro.and(FocoFogoSpecs.bioma(bioma.trim()));
        }
        if (preenchido(satelite)) {
            filtro = filtro.and(FocoFogoSpecs.satelite(satelite.trim().toUpperCase()));
        }
        if (dataInicio != null) {
            filtro = filtro.and(FocoFogoSpecs.aPartirDe(dataInicio.atStartOfDay()));
        }
        if (dataFim != null) {
            filtro = filtro.and(FocoFogoSpecs.ate(dataFim.plusDays(1).atStartOfDay()));
        }
        return filtro;
    }

    public Optional<FocoFogo> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public FocoFogo criar(FocoFogo foco) {
        padronizar(foco);
        if (repository.existsByLatitudeAndLongitudeAndDataHoraAndSatelite(
                foco.getLatitude(), foco.getLongitude(), foco.getDataHora(), foco.getSatelite())) {
            throw new RegistroInvalidoException(MSG_DUPLICADO);
        }
        foco.setId(null);
        return repository.save(foco);
    }

    public Optional<FocoFogo> atualizar(Long id, FocoFogo dados) {
        Optional<FocoFogo> existente = repository.findById(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }
        padronizar(dados);
        if (repository.existsByLatitudeAndLongitudeAndDataHoraAndSateliteAndIdNot(
                dados.getLatitude(), dados.getLongitude(), dados.getDataHora(), dados.getSatelite(), id)) {
            throw new RegistroInvalidoException(MSG_DUPLICADO);
        }
        FocoFogo foco = existente.get();
        foco.setDataHora(dados.getDataHora());
        foco.setSatelite(dados.getSatelite());
        foco.setUf(dados.getUf());
        foco.setMunicipio(dados.getMunicipio());
        foco.setBioma(dados.getBioma());
        foco.setLatitude(dados.getLatitude());
        foco.setLongitude(dados.getLongitude());
        foco.setRiscoFogo(dados.getRiscoFogo());
        return Optional.of(repository.save(foco));
    }

    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private boolean preenchido(String texto) {
        return texto != null && !texto.isBlank();
    }

    private void padronizar(FocoFogo foco) {
        foco.setSatelite(foco.getSatelite().trim().toUpperCase());
        foco.setUf(foco.getUf().trim().toUpperCase());
        foco.setMunicipio(foco.getMunicipio().trim().toUpperCase());
        foco.setBioma(foco.getBioma().trim());
        if (!Estados.siglaValida(foco.getUf())) {
            throw new RegistroInvalidoException("UF inválida: " + foco.getUf());
        }
    }
}
