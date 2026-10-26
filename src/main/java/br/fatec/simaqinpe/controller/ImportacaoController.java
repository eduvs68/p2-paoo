package br.fatec.simaqinpe.controller;

import br.fatec.simaqinpe.service.ImportacaoService;
import br.fatec.simaqinpe.service.ResumoImportacao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/focos/importar")
public class ImportacaoController {

    private final ImportacaoService service;

    public ImportacaoController(ImportacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResumoImportacao> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(service.importar(arquivo));
    }
}
