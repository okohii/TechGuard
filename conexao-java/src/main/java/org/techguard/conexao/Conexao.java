package org.techguard.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Conexao {
    private static final Logger LOGGER = LogManager.getLogger(Conexao.class);

    private static final String url = "jdbc:mysql://TechGuardDB/techguard"; // adicionando o prefixo jdbc:mysql://
    private static final String user = "root";
    private static final String password = "solutions";

    private static Connection conn;

    public static Connection getConexao() throws ErroNaConexaoBanco {
        try {
            if (conn == null || conn.isClosed()) { // Verifica se a conexão é nula ou foi fechada
                LOGGER.debug("Criando nova conexão com o banco de dados. URL: {}, Usuário: {}", url, user);
                conn = DriverManager.getConnection(url, user, password);
                LOGGER.info("Conexão com o banco de dados estabelecida com sucesso.");
                return conn;
            } else {
                LOGGER.debug("Utilizando conexão com o banco de dados existente.");
                return conn;
            }
        } catch (SQLException e) {
            LOGGER.error("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
            throw new ErroNaConexaoBanco("Erro ao estabelecer conexão com o banco de dados: " + e.getMessage(), e);
        } finally {
            System.out.println("A operação de tentativa de conexão com o banco foi finalizada!");
        }
    }
}