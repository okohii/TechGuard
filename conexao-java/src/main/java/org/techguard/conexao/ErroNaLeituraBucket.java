package org.techguard.conexao;
import java.io.IOException;

public class ErroNaLeituraBucket extends IOException {
    public void leituraNaoRealizada(){
        System.out.println("Erro ao realizar a leitura do Bucket!");
    }
}
