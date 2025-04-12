package org.techguard.tratativa;

import org.apache.poi.ss.usermodel.Workbook;
import org.techguard.api.ClassificadorAPI;
import org.techguard.conexao.S3Connection;
import org.techguard.modelo.Incidente;

import java.io.IOException;
import java.util.List;

public abstract class TratadorDados {
    protected S3Connection s3Connection;
    protected ClassificadorAPI classificadorAPI;

    public TratadorDados(S3Connection s3Connection, ClassificadorAPI classificadorAPI) {
        this.s3Connection = s3Connection;
        this.classificadorAPI = classificadorAPI;
    }

    public abstract List<Incidente> processarDados(Workbook workbook) throws IOException;

    public abstract List<Incidente> processarDados(Workbook workbook, List<Incidente> incidentes) throws IOException;
}


