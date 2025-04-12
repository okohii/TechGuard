package org.techguard.tratativa;


import org.apache.poi.ss.usermodel.*;
import org.techguard.api.ClassificadorAPI;
import org.techguard.conexao.S3Connection;
import org.techguard.modelo.Incidente;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TratadorDadosIncidentes extends TratadorDados {
    private static final Logger LOGGER = LogManager.getLogger(TratadorDadosIncidentes.class);

    public TratadorDadosIncidentes(S3Connection s3Connection, ClassificadorAPI classificadorAPI) {
        super(s3Connection, classificadorAPI);
    }


    @Override
    public List<Incidente> processarDados(Workbook workbook) throws IOException {
        return processarDados(workbook, new ArrayList<>()); // Chama o outro método com uma nova lista
    }

    @Override
    public List<Incidente> processarDados(Workbook workbook, List<Incidente> incidentes) throws IOException {
        LOGGER.info("Iniciando o processamento dos dados de incidentes.");
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd");

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            LOGGER.info("Processando dados da planilha: " + sheet.getSheetName());

            for (int rowIndex = 1; rowIndex <= 2; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    Incidente incidente = new Incidente();

                    try {
                        Cell cellData = row.getCell(0);
                        if (cellData != null && cellData.getCellType() == CellType.NUMERIC) {
                            Date data = cellData.getDateCellValue();
                            incidente.setData(data);
                        }

                        Cell cellNome = row.getCell(1);
                        if (cellNome != null && cellNome.getCellType() == CellType.STRING) {
                            String nome = cellNome.getStringCellValue();
                            incidente.setNome(nome);
                        }

                        Cell cellTipo = row.getCell(2);
                        if (cellTipo != null && cellTipo.getCellType() == CellType.STRING) {
                            String tipo = cellTipo.getStringCellValue();
                            incidente.setTipoIncidente(tipo);
                        }

                        incidentes.add(incidente);
                        LOGGER.debug("Incidente adicionado: {}", incidente);

                    } catch (Exception e) {
                        System.err.println("Erro ao processar linha " + rowIndex + ": " + e.getMessage());
                    }
                }
            }
        }
        LOGGER.info("Processamento dos dados de incidentes concluído. {} incidentes encontrados.", incidentes.size());
        return incidentes;
    }
}