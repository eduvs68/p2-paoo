package br.fatec.simaqinpe.repository;

import br.fatec.simaqinpe.service.FocoLinha;
import java.sql.Types;
import java.util.List;
import java.util.StringJoiner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FocoFogoImportacaoRepository {

    private static final String INSERT = "INSERT INTO foco_fogo "
            + "(data_hora, satelite, uf, municipio, bioma, latitude, longitude, risco_fogo) VALUES ";
    private static final String PARAMETROS_LINHA = "(?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbc;

    public FocoFogoImportacaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void limparTabela() {
        jdbc.execute("TRUNCATE TABLE foco_fogo RESTART IDENTITY");
    }

    public int inserirLote(List<FocoLinha> linhas) {
        StringJoiner valores = new StringJoiner(", ", INSERT, " ON CONFLICT DO NOTHING");
        linhas.forEach(linha -> valores.add(PARAMETROS_LINHA));

        return jdbc.update(valores.toString(), ps -> {
            int i = 1;
            for (FocoLinha linha : linhas) {
                ps.setObject(i++, linha.dataHora());
                ps.setString(i++, linha.satelite());
                ps.setString(i++, linha.uf());
                ps.setString(i++, linha.municipio());
                ps.setString(i++, linha.bioma());
                ps.setBigDecimal(i++, linha.latitude());
                ps.setBigDecimal(i++, linha.longitude());
                if (linha.riscoFogo() == null) {
                    ps.setNull(i++, Types.NUMERIC);
                } else {
                    ps.setBigDecimal(i++, linha.riscoFogo());
                }
            }
        });
    }
}
