package org.techguard.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassificadorAPI {
    private static final Logger LOGGER = LogManager.getLogger(ClassificadorAPI.class);

    private String apiKey;

    public ClassificadorAPI(String apiKey) {
        this.apiKey = apiKey;
    }

    public String classificar(String termo, String categoria) throws IOException, InterruptedException {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=AIzaSyAzB0MJCPbIT9wyX69NbqkIIO7okm5HRgk";
        String prompt = gerarPrompt(termo, categoria);
        String jsonInputString = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", prompt);
        LOGGER.info("Chamando API para classificar termo '{}' na categoria '{}'. URL: {}", termo, categoria, endpoint);

        int tentativas = 0;
        while (tentativas < 10) {
            try {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    }

                    LOGGER.debug("Resposta completa da API: {}", response.toString()); // Log da resposta completa
                    return extrairClassificacao(response.toString());

                } else if (responseCode == 429) {
                    LOGGER.warn("API retornou 'Too Many Requests'. Tentando novamente em {} segundos...", (long) Math.pow(2, tentativas));// Too Many Requests
                    Thread.sleep((long) Math.pow(2, tentativas) * 1000); // Backoff exponencial
                    tentativas++;
                }
                else {
                    String mensagemErro = String.format("Erro na chamada da API. Código de resposta: %d, Termo: %s, Categoria: %s", responseCode, termo, categoria);
                    LOGGER.error(mensagemErro);
                    throw new IOException("Erro na chamada da API: " + responseCode);
                }
            } catch (IOException e) {
                if (tentativas < 9) { // Retenta apenas 9x
                    tentativas++;
                    Thread.sleep(1000 * (long) Math.pow(2, tentativas)); // Backoff exponencial
                    continue;
                }
                throw new IOException("Excedeu o número máximo de tentativas. Erro original: " + e.getMessage(), e);
            }
        }
        LOGGER.error("Falha ao classificar termo '{}' na categoria '{}' após {} tentativas.", termo, categoria, tentativas);
        return "Classification falhou"; // ou lance uma exceção
    }

    private String gerarPrompt(String termo, String categoria) {
        // Lógica para gerar o prompt correto com base na categoria
        switch (categoria) {
            case "Affect":
                return String.format("Classify the term: '%s' into one of the following categories:\n1. Software and Applications\n2. Malware and Vulnerabilities\n3. Frameworks and Libraries\n4. Hardware and Firmware\n5. Protocols and APIs\n6. Development Tools and Packages\nRespond with the category name only(without numbers or special characters).", termo);
            case "Downstream Target":
                return String.format("Classify the term: '%s' into one of the following categories:\n1. Systems and Platform Users\n2. Software Applications and Libraries\n3. Companies and organizations\n4. Cryptocurrency and Finance Users\n5. Governments, Activists and Non-Governmental Organizations (NGOs)\n6. Developers and IT Professionals\nRespond with the category name only(without numbers or special characters).", termo);
            case "Impact":
                return String.format("Classify the term: '%s' into one of the following categories:\n1. Data Extraction\n2. Remote Code Execution\n3. Backdoor Access\n4. Data Damage\n5. Payment Diversion\nRespond with the category name only(without numbers or special characters).", termo);
            case "Prevenção":
                return "Como posso me prevenir de ataques na minha empresa?";
            case "Leis":
                return "Quais leis impedem que eu seja hackeado?";
            case "Detecção":
                return "Como saber se minhas informações foram hackeadas?";
            default:
                return "";
        }
    }

    private String extrairClassificacao(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray candidatesArray = jsonObject.getJSONArray("candidates");
        if (candidatesArray.length() > 0) {
            JSONObject candidate = candidatesArray.getJSONObject(0);
            if (candidate.getString("finishReason").equals("STOP")) {
                if (candidate.has("content")) {
                    JSONObject content = candidate.getJSONObject("content");
                    JSONArray parts = content.getJSONArray("parts");
                    if (parts.length() > 0) {
                        return parts.getJSONObject(0).getString("text").trim();
                    }
                }
            }else {
                return "Data Damage";
            }
        }
        return "Data Damage";
    }
}