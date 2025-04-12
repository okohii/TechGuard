package org.techguard.dao;

import org.techguard.conexao.ErroNaConexaoBanco;
import org.techguard.modelo.Incidente;

import java.sql.*;
import java.util.List;

import org.techguard.conexao.Conexao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LeituraETratativaDAO {
    private static final Logger LOGGER = LogManager.getLogger(LeituraETratativaDAO.class);

    public void cadastrarDados(List<Incidente> incidentes) throws ErroNaConexaoBanco {
        LOGGER.info("Iniciando a persistência dos dados no banco de dados.");

        String sql = "INSERT INTO registros (data, nome, attack_ou_disclosure, modificados_affect, modificados_downstream_target, modificados_impact) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlResposta = "INSERT INTO respostaIA (fkPerguntaIA, resultado) VALUES (?, ?)";
        String sqlPrompt = "INSERT INTO perguntaIA (prompt) VALUES (?)";
        String truncate = "TRUNCATE TABLE registros";
        String truncateResposta = "TRUNCATE TABLE respostaIA";
        String truncatePergunta = "TRUNCATE TABLE perguntaIA";
        String selectPerguntaId = "SELECT idPerguntaIA FROM perguntaIA WHERE prompt = ?";

        try (Connection conexao = Conexao.getConexao()) {
            try (Statement statement = conexao.createStatement()) {
                statement.execute("SET FOREIGN_KEY_CHECKS = 0");

                try (PreparedStatement psTruncate = conexao.prepareStatement(truncate);
                     PreparedStatement psTruncatePergunta = conexao.prepareStatement(truncatePergunta);
                     PreparedStatement psTruncateResposta = conexao.prepareStatement(truncateResposta);
                     PreparedStatement ps = conexao.prepareStatement(sql);
                     PreparedStatement psResposta = conexao.prepareStatement(sqlResposta);
                     PreparedStatement psPrompt = conexao.prepareStatement(sqlPrompt);
                     PreparedStatement psSelectPergunta = conexao.prepareStatement(selectPerguntaId)
                ) {

                    psTruncate.executeUpdate();
                    psTruncateResposta.executeUpdate();
                    psTruncatePergunta.executeUpdate();
                    LOGGER.info("Tabela 'registros' truncada com sucesso.");

                    statement.execute("SET FOREIGN_KEY_CHECKS = 1");

                    for (Incidente incidente : incidentes) {
                        ps.setDate(1, new java.sql.Date(incidente.getData().getTime())); // Converta para java.sql.Date
                        ps.setString(2, incidente.getNome());
                        ps.setString(3, incidente.getTipoIncidente());
                        ps.setString(4, incidente.getAffected());
                        ps.setString(5, incidente.getDownstreamTarget());
                        ps.setString(6, incidente.getImpact());
                        ps.executeUpdate();
                        LOGGER.debug("Incidente inserido no banco de dados: {}", incidente);
                    }

                    String[] perguntas = {
                            "Como posso me prevenir de ataques na minha empresa?",
                            "Quais leis impedem que eu seja hackeado?",
                            "Como saber se minhas informações foram hackeadas?"
                    };

                    for (int i = 0; i < perguntas.length; i++) {
                        psPrompt.setString(1, perguntas[i]);
                        psPrompt.executeUpdate();

                        psSelectPergunta.setString(1, perguntas[i]);
                        try (ResultSet rs = psSelectPergunta.executeQuery()) {
                            if (rs.next()) {
                                int idPergunta = rs.getInt("idPerguntaIA");


                                String resposta = obterRespostaParaPergunta(incidentes.get(i % incidentes.size()), perguntas[i]);


                                psResposta.setInt(1, idPergunta);
                                psResposta.setString(2, resposta);
                                psResposta.executeUpdate();
                                LOGGER.debug("Resposta inserida no banco de dados. Pergunta: {}, Resposta: {}", perguntas[i], resposta);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("Erro de SQL: " + e.getMessage(), e);
            }
            LOGGER.info("Persistência dos dados no banco de dados concluída.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private String obterRespostaParaPergunta(Incidente incidente, String pergunta) {
        switch (pergunta) {
            case "Como posso me prevenir de ataques na minha empresa?":
                return incidente.getPrevencao();
            case "Quais leis impedem que eu seja hackeado?":
                return incidente.getLeis();
            case "Como saber se minhas informações foram hackeadas?":
                return incidente.getDeteccao();
            default:
                return null; // Ou lance uma exceção
        }
    }
    }
