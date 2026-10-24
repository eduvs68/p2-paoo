package br.fatec.simaqinpe.service;

import java.util.Map;

public final class Estados {

    private static final Map<String, String> SIGLAS = Map.ofEntries(
            Map.entry("ACRE", "AC"),
            Map.entry("ALAGOAS", "AL"),
            Map.entry("AMAPÁ", "AP"),
            Map.entry("AMAZONAS", "AM"),
            Map.entry("BAHIA", "BA"),
            Map.entry("CEARÁ", "CE"),
            Map.entry("DISTRITO FEDERAL", "DF"),
            Map.entry("ESPÍRITO SANTO", "ES"),
            Map.entry("GOIÁS", "GO"),
            Map.entry("MARANHÃO", "MA"),
            Map.entry("MATO GROSSO", "MT"),
            Map.entry("MATO GROSSO DO SUL", "MS"),
            Map.entry("MINAS GERAIS", "MG"),
            Map.entry("PARÁ", "PA"),
            Map.entry("PARAÍBA", "PB"),
            Map.entry("PARANÁ", "PR"),
            Map.entry("PERNAMBUCO", "PE"),
            Map.entry("PIAUÍ", "PI"),
            Map.entry("RIO DE JANEIRO", "RJ"),
            Map.entry("RIO GRANDE DO NORTE", "RN"),
            Map.entry("RIO GRANDE DO SUL", "RS"),
            Map.entry("RONDÔNIA", "RO"),
            Map.entry("RORAIMA", "RR"),
            Map.entry("SANTA CATARINA", "SC"),
            Map.entry("SÃO PAULO", "SP"),
            Map.entry("SERGIPE", "SE"),
            Map.entry("TOCANTINS", "TO"));

    private Estados() {
    }

    public static String paraSigla(String nome) {
        return SIGLAS.get(nome.trim().toUpperCase());
    }

    public static boolean siglaValida(String sigla) {
        return SIGLAS.containsValue(sigla);
    }
}
