package org.techguard;

import com.slack.api.methods.SlackApiException;
import io.github.cdimascio.dotenv.Dotenv;
import org.techguard.api.ClassificadorAPI;
import org.techguard.conexao.ErroNaConexaoBanco;
import org.techguard.slack.slack;
import org.techguard.conexao.S3Connection;
import org.techguard.dao.LeituraETratativaDAO;
import org.techguard.modelo.Incidente;
import org.techguard.tratativa.ProcessadorDados;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Principal {
    private static final Logger LOGGER = LogManager.getLogger(Principal.class);

    public static void main(String[] args) throws IOException {
        LOGGER.info("Iniciando a aplicação Principal.");

        String bucketName = "bucket-base-de-dados";
        String key = "basededados.xlsx";
        String gemini = "AIzaSyAzB0MJCPbIT9wyX69NbqkIIO7okm5HRgk\n";
        Region region = Region.US_EAST_1;

        try (S3Connection s3Connection = new S3Connection(bucketName, key, region)) {
            LOGGER.info("Conexão com o S3 estabelecida.");

            ClassificadorAPI classificadorAPI = new ClassificadorAPI(gemini);
            ProcessadorDados processador = new ProcessadorDados(s3Connection, classificadorAPI);

            LOGGER.info("Iniciando o processamento dos dados.");
            List<Incidente> incidentes = processador.processar();
            LOGGER.info("Processamento dos dados concluído. {} incidentes processados.", incidentes.size());

            LeituraETratativaDAO dao = new LeituraETratativaDAO();
            LOGGER.info("Iniciando a persistência dos dados no banco de dados.");

            try {
                slack.enviarMensagemSlack("Processamento concluído com sucesso!\n Agora você poderá analisar os dados em sua dashboard personalizada0\nAcesse: techguard.ddns.net:8080 .");
                LOGGER.info("Mensagem enviada para o Slack com sucesso.");
            } catch (IOException | SlackApiException e) {
                LOGGER.error("Erro ao enviar mensagem para o Slack: {}", e.getMessage(), e);
            }

            try{
                dao.cadastrarDados(incidentes);
                LOGGER.info("Dados persistidos no banco de dados com sucesso.");
            }catch (RuntimeException e){
                LOGGER.error("Erro ao persistir dados no banco de dados: " + e.getMessage(), e);
            } catch (ErroNaConexaoBanco e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            LOGGER.error("Erro ao processar dados: " + e.getMessage(), e);
        }
        LOGGER.info("Finalizando a aplicação Principal.");
    }
}