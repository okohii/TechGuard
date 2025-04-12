package org.techguard.conexao;

public class ErroNaConexaoBanco extends Exception {
    public ErroNaConexaoBanco(String message, Throwable cause) {
        super(message, cause);
    }
}