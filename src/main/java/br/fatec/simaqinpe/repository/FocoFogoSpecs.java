package br.fatec.simaqinpe.repository;

import br.fatec.simaqinpe.model.FocoFogo;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class FocoFogoSpecs {

    private FocoFogoSpecs() {
    }

    public static Specification<FocoFogo> uf(String uf) {
        return (root, query, cb) -> cb.equal(root.get("uf"), uf);
    }

    public static Specification<FocoFogo> bioma(String bioma) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("bioma")), bioma.toLowerCase());
    }

    public static Specification<FocoFogo> satelite(String satelite) {
        return (root, query, cb) -> cb.equal(root.get("satelite"), satelite);
    }

    public static Specification<FocoFogo> aPartirDe(LocalDateTime inicio) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataHora"), inicio);
    }

    public static Specification<FocoFogo> ate(LocalDateTime fim) {
        return (root, query, cb) -> cb.lessThan(root.get("dataHora"), fim);
    }
}
