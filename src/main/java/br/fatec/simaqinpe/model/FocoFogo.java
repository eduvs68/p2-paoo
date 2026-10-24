package br.fatec.simaqinpe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "foco_fogo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"latitude", "longitude", "data_hora", "satelite"}))
@Getter
@Setter
@NoArgsConstructor
public class FocoFogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A data e hora são obrigatórias")
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @NotBlank(message = "O satélite é obrigatório")
    @Size(max = 30, message = "O satélite deve ter no máximo 30 caracteres")
    @Column(nullable = false, length = 30)
    private String satelite;

    @NotBlank(message = "A UF é obrigatória")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "A UF deve ter 2 letras")
    @Column(nullable = false, length = 2)
    private String uf;

    @NotBlank(message = "O município é obrigatório")
    @Size(max = 100, message = "O município deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String municipio;

    @NotBlank(message = "O bioma é obrigatório")
    @Size(max = 30, message = "O bioma deve ter no máximo 30 caracteres")
    @Column(nullable = false, length = 30)
    private String bioma;

    @NotNull(message = "A latitude é obrigatória")
    @DecimalMin(value = "-90", message = "A latitude deve estar entre -90 e 90")
    @DecimalMax(value = "90", message = "A latitude deve estar entre -90 e 90")
    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @NotNull(message = "A longitude é obrigatória")
    @DecimalMin(value = "-180", message = "A longitude deve estar entre -180 e 180")
    @DecimalMax(value = "180", message = "A longitude deve estar entre -180 e 180")
    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @DecimalMin(value = "0", message = "O risco de fogo deve estar entre 0 e 1")
    @DecimalMax(value = "1", message = "O risco de fogo deve estar entre 0 e 1")
    @Column(name = "risco_fogo", precision = 4, scale = 2)
    private BigDecimal riscoFogo;
}
