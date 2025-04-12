package org.techguard.conexao;
import java.sql.SQLException;

public class ErroNoSelect extends SQLException {
    public void mostrarErroNoSelect(){
        System.out.println("Falha na tentativa de realizar select no banco de dados");
    }
}
