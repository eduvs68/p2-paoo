package br.fatec.simaqinpe.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FocoLinha(
        LocalDateTime dataHora,
        String satelite,
        String uf,
        String municipio,
        String bioma,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal riscoFogo) {
}
