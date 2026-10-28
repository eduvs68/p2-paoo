package br.fatec.simaqinpe.controller;

import br.fatec.simaqinpe.model.FocoFogo;
import br.fatec.simaqinpe.service.ContagemMensal;
import br.fatec.simaqinpe.service.FocoFogoService;
import br.fatec.simaqinpe.service.OpcoesFiltro;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/focos")
public class FocoFogoController {

    private final FocoFogoService service;

    public FocoFogoController(FocoFogoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<FocoFogo>> listar(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/filtro")
    public ResponseEntity<Page<FocoFogo>> filtrar(
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) String bioma,
            @RequestParam(required = false) String satelite,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.filtrar(uf, bioma, satelite, dataInicio, dataFim, pageable));
    }

    @GetMapping("/estatisticas/mensal")
    public ResponseEntity<List<ContagemMensal>> contarPorMes(
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) String bioma,
            @RequestParam(required = false) String satelite,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return ResponseEntity.ok(service.contarPorMes(uf, bioma, satelite, dataInicio, dataFim));
    }

    @GetMapping("/opcoes")
    public ResponseEntity<OpcoesFiltro> listarOpcoes() {
        return ResponseEntity.ok(service.listarOpcoes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> naoEncontrado(id));
    }

    @PostMapping
    public ResponseEntity<FocoFogo> criar(@Valid @RequestBody FocoFogo foco) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(foco));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody FocoFogo foco) {
        return service.atualizar(id, foco)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> naoEncontrado(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (service.excluir(id)) {
            return ResponseEntity.noContent().build();
        }
        return naoEncontrado(id);
    }

    private ResponseEntity<?> naoEncontrado(Long id) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResposta("Foco de fogo não encontrado: id " + id));
    }
}
