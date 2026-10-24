package br.fatec.simaqinpe.service;

public class RegistroInvalidoException extends RuntimeException {

    public RegistroInvalidoException(String mensagem) {
        super(mensagem);
    }
}
