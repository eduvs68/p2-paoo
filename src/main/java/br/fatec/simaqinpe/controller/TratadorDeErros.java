package br.fatec.simaqinpe.controller;

import br.fatec.simaqinpe.service.RegistroInvalidoException;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ErroResposta(mensagem));
    }

    @ExceptionHandler(RegistroInvalidoException.class)
    public ResponseEntity<ErroResposta> registroInvalido(RegistroInvalidoException e) {
        return ResponseEntity.badRequest().body(new ErroResposta(e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoIlegivel(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new ErroResposta("JSON inválido ou com valores em formato incorreto"));
    }
}
