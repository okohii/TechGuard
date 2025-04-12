
package org.techguard.conexao;
import java.sql.SQLException;

public class ErroNoInsert extends SQLException {

    public void mostrarErro(){
        System.out.println("Erro ao inserir dados no banco de dados!");
    }
}
