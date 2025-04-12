package org.techguard.conexao;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;

public class S3Connection implements AutoCloseable {
    private S3Client s3Client;
    private String bucketName;
    private String key;
    private Region region;

    public S3Connection(String bucketName, String key, Region region) {
        this.bucketName = bucketName;
        this.key = key;
        this.region = region;
        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    public Workbook getWorkbook() throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (InputStream inputStream = s3Client.getObject(getObjectRequest)) {
            return new XSSFWorkbook(inputStream);
        } catch (S3Exception | IOException e) {
            throw new IOException("Erro na leitura do bucket: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }
}