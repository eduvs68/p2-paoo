package br.fatec.simaqinpe.repository;

import br.fatec.simaqinpe.model.FocoFogo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocoFogoRepository extends JpaRepository<FocoFogo, Long> {

    boolean existsByLatitudeAndLongitudeAndDataHoraAndSatelite(
            BigDecimal latitude, BigDecimal longitude, LocalDateTime dataHora, String satelite);

    boolean existsByLatitudeAndLongitudeAndDataHoraAndSateliteAndIdNot(
            BigDecimal latitude, BigDecimal longitude, LocalDateTime dataHora, String satelite, Long id);
}
