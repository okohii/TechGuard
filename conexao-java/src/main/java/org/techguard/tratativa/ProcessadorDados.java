package org.techguard.tratativa;

import org.apache.poi.ss.usermodel.Workbook;
import org.techguard.api.ClassificadorAPI;
import org.techguard.conexao.S3Connection;
import org.techguard.modelo.Incidente;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessadorDados {
    private S3Connection s3Connection;
    private ClassificadorAPI classificadorAPI;
    private TratadorDadosIncidentes tratadorDadosIncidentes;
    private TratadorDadosClassificacao tratadorDadosClassificacao;


    public ProcessadorDados(S3Connection s3Connection, ClassificadorAPI classificadorAPI) {
        this.s3Connection = s3Connection;
        this.classificadorAPI = classificadorAPI;
        this.tratadorDadosIncidentes = new TratadorDadosIncidentes(s3Connection, classificadorAPI);
        this.tratadorDadosClassificacao = new TratadorDadosClassificacao(s3Connection, classificadorAPI);
    }

    private static final Logger LOGGER = LogManager.getLogger(ProcessadorDados.class);

    public List<Incidente> processar() throws IOException {
        LOGGER.info("Iniciando o processamento dos dados.");

        try (Workbook workbook = s3Connection.getWorkbook()) {
            LOGGER.info("Workbook obtido com sucesso do S3.");

            List<Incidente> incidentes = new ArrayList<>();
            LOGGER.info("Processando dados de incidentes.");
            incidentes = tratadorDadosIncidentes.processarDados(workbook, incidentes);
            LOGGER.info("Dados de incidentes processados.");

            LOGGER.info("Classificando dados.");
            incidentes = tratadorDadosClassificacao.processarDados(workbook, incidentes);
            LOGGER.info("Dados classificados.");
            LOGGER.info("Processamento concluído.");

            return incidentes;

        }
    }
}
