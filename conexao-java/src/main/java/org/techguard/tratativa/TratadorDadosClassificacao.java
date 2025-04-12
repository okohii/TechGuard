package org.techguard.tratativa;

import org.apache.poi.ss.usermodel.*;
import org.techguard.api.ClassificadorAPI;
import org.techguard.conexao.S3Connection;
import org.techguard.modelo.Incidente;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TratadorDadosClassificacao extends TratadorDados {
    private static final Logger LOGGER = LogManager.getLogger(TratadorDadosClassificacao.class);

    private static final int COLUNA_AFFECT = 5;
    private static final int COLUNA_DOWNSTREAM = 7;
    private static final int COLUNA_IMPACT = 49;

    public TratadorDadosClassificacao(S3Connection s3Connection, ClassificadorAPI classificadorAPI) {
        super(s3Connection, classificadorAPI);
    }

    @Override
    public List<Incidente> processarDados(Workbook workbook) throws IOException {
        return new ArrayList<>();
    }


    @Override
    public List<Incidente> processarDados(Workbook workbook, List<Incidente> incidentes) throws IOException {
        LOGGER.info("Iniciando classificação dos dados.");

        if (incidentes == null || incidentes.isEmpty()) {
            return incidentes;
        }
        try {
            // Chama os prompts extras apenas uma vez
            String prevencao = classificadorAPI.classificar(null, "Prevenção");
            String leis = classificadorAPI.classificar(null, "Leis");
            String deteccao = classificadorAPI.classificar(null, "Detecção");


            LOGGER.info("Resposta para o prompt extra 1: {}", prevencao);
            LOGGER.info("Resposta para o prompt extra 2: {}", leis);
            LOGGER.info("Resposta para o prompt extra 3: {}", deteccao);

            for (Incidente incidente : incidentes) {
                incidente.setPrevencao(prevencao); // Define o campo com a resposta
                incidente.setLeis(leis);
                incidente.setDeteccao(deteccao);

            }

        } catch (InterruptedException e) {
            LOGGER.error("Erro ao chamar os prompts extras: {}", e.getMessage(), e);
            // Trate a exceção conforme necessário (retornar null, lançar exceção, etc.)
            return null;
        }
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            for (int rowIndex = 1; rowIndex <= 2; rowIndex++) {

                if (rowIndex - 1 >= incidentes.size()) {
                    continue; // Pula se não há incidente para essa linha
                }

                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                Incidente incidente = incidentes.get(rowIndex - 1);

                try {
                    LOGGER.debug("Classificando incidente na linha {}", rowIndex);
                    classificarCampo(row, incidente, COLUNA_AFFECT, "Affect");
                    classificarCampo(row, incidente, COLUNA_DOWNSTREAM, "Downstream Target");
                    classificarCampo(row, incidente, COLUNA_IMPACT, "Impact");


                } catch (IOException | InterruptedException e) {
                    System.err.println("Erro ao classificar campo na linha " + rowIndex + ": " + e.getMessage());
                }
            }
        }
        return incidentes;
    }

    private void classificarCampo(Row row, Incidente incidente, int coluna, String nomeCampo) throws IOException, InterruptedException {
        Cell cell = row.getCell(coluna);
        if (cell != null && cell.getCellType() == CellType.STRING) {
            String term = cell.getStringCellValue();
            String classificacao = classificadorAPI.classificar(term, nomeCampo);
            switch (nomeCampo) {
                case "Affect":
                    incidente.setAffected(classificacao);
                    break;
                case "Downstream Target":
                    incidente.setDownstreamTarget(classificacao);
                    break;
                case "Impact":
                    incidente.setImpact(classificacao);
                    break;
            }
        }
    }
}