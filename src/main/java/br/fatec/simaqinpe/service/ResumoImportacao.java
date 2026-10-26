package br.fatec.simaqinpe.service;

import java.util.Map;

public record ResumoImportacao(
        long linhasLidas,
        long importados,
        long rejeitados,
        long duplicados,
        Map<String, Long> motivosRejeicao,
        String mensagem) {
}
