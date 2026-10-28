package br.fatec.simaqinpe.repository;

import br.fatec.simaqinpe.model.FocoFogo;
import br.fatec.simaqinpe.service.ContagemMensal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.LocalDateTimeField;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class FocoFogoEstatisticasRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<ContagemMensal> contarPorMes(Specification<FocoFogo> filtro) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ContagemMensal> consulta = cb.createQuery(ContagemMensal.class);
        Root<FocoFogo> foco = consulta.from(FocoFogo.class);

        Expression<LocalDateTime> dataHora = foco.get("dataHora");
        Expression<Integer> ano = cb.extract(LocalDateTimeField.YEAR, dataHora);
        Expression<Integer> mes = cb.extract(LocalDateTimeField.MONTH, dataHora);
        consulta.select(cb.construct(ContagemMensal.class, ano, mes, cb.count(foco)));

        Predicate condicao = filtro.toPredicate(foco, consulta, cb);
        if (condicao != null) {
            consulta.where(condicao);
        }
        consulta.groupBy(ano, mes).orderBy(cb.asc(ano), cb.asc(mes));

        return entityManager.createQuery(consulta).getResultList();
    }
}
