package br.fatec.simaqinpe.controller;

import br.fatec.simaqinpe.service.RegistroInvalidoException;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(new ErroResposta("Valor inválido para o parâmetro '" + e.getName() + "'"));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErroResposta> arquivoAusente(MissingServletRequestPartException e) {
        return ResponseEntity.badRequest().body(new ErroResposta("Nenhum arquivo foi enviado"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErroResposta> requisicaoNaoMultipart(MultipartException e) {
        return ResponseEntity.badRequest()
                .body(new ErroResposta("A requisição deve ser multipart/form-data com o campo 'arquivo'"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroResposta> arquivoGrande(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest().body(new ErroResposta("O arquivo excede o tamanho máximo de 500 MB"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoIlegivel(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new ErroResposta("JSON inválido ou com valores em formato incorreto"));
    }
}
