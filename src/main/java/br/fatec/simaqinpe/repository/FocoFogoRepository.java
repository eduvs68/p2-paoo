package br.fatec.simaqinpe.repository;

import br.fatec.simaqinpe.model.FocoFogo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface FocoFogoRepository extends JpaRepository<FocoFogo, Long>, JpaSpecificationExecutor<FocoFogo> {

    @Query("select distinct f.bioma from FocoFogo f order by f.bioma")
    List<String> listarBiomas();

    @Query("select distinct f.satelite from FocoFogo f order by f.satelite")
    List<String> listarSatelites();

    boolean existsByLatitudeAndLongitudeAndDataHoraAndSatelite(
            BigDecimal latitude, BigDecimal longitude, LocalDateTime dataHora, String satelite
    );

    boolean existsByLatitudeAndLongitudeAndDataHoraAndSateliteAndIdNot(
            BigDecimal latitude, BigDecimal longitude, LocalDateTime dataHora, String satelite, Long id
    );
}
