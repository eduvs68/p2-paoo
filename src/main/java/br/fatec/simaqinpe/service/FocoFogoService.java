package br.fatec.simaqinpe.service;

import br.fatec.simaqinpe.model.FocoFogo;
import br.fatec.simaqinpe.repository.FocoFogoRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FocoFogoService {

    private static final String MSG_DUPLICADO = "Já existe um foco com a mesma data, satélite e coordenadas";

    private final FocoFogoRepository repository;

    public FocoFogoService(FocoFogoRepository repository) {
        this.repository = repository;
    }

    public Page<FocoFogo> listar(Pageable pageable) {
        return repository.findAll(pageable);
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
