package com.mushroom.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mushroom.server.model.MushroomReference;
import com.mushroom.server.repository.MushroomRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AiService {

    // Если Python в Docker, а Java нет — localhost:8000
    // Если оба в Docker — http://ai-service:8000/predict
    // Если переменная среды AI_HOST задана (в Docker), используем её. Иначе localhost.
    private static final String AI_HOST = System.getenv("AI_HOST") != null ? System.getenv("AI_HOST") : "localhost";
    private static final String AI_URL = "http://" + AI_HOST + ":8000/predict";

    private final MushroomRepository mushroomRepository;

    public RecognitionResult recognize(MultipartFile file) throws IOException {
        String boundary = "---" + System.currentTimeMillis() + "---";
        String CRLF = "\r\n";

        URL url = new URL(AI_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("User-Agent", "Java-Client");

        // --- 1. ОТПРАВКА ФАЙЛА В PYTHON ---
        try (OutputStream output = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {

            // Заголовок части файла
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"").append(CRLF);
            writer.append("Content-Type: " + (file.getContentType() != null ? file.getContentType() : "application/octet-stream")).append(CRLF);
            writer.append(CRLF).flush();

            // Тело файла
            InputStream fileInput = file.getInputStream();
            fileInput.transferTo(output); // Удобный метод для перекачки байтов
            output.flush();
            fileInput.close();

            // Конец запроса
            writer.append(CRLF).flush();
            writer.append("--" + boundary + "--").append(CRLF).flush();
        }

        // --- 2. ЧТЕНИЕ ОТВЕТА ---
        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            // Успешный ответ от Python
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) response.append(inputLine);
            in.close();

            // Парсим JSON от Python: {"class": "penny_bun", "confidence": 98.5}
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());

            String aiKey = root.get("class").asText();
            double confidence = root.get("confidence").asDouble();

            // --- 3. ПОИСК В БАЗЕ ДАННЫХ ---
            MushroomReference ref = mushroomRepository.findByName(aiKey).orElse(null);

            // --- 4. СБОРКА РЕЗУЛЬТАТА ---
            RecognitionResult result = new RecognitionResult();
            result.setAiKey(aiKey);
            result.setConfidence(confidence);

            if (ref != null) {
                result.setFound(true);
                result.setDisplayName(ref.getDisplayName());
                result.setDescription(ref.getDescription());
                result.setEdible(ref.getIsEdible());
                result.setCookingTips(ref.getCookingTips());
            } else {
                result.setFound(false);
                result.setDisplayName("Неизвестный гриб (" + aiKey + ")");
                result.setDescription("К сожалению, информации об этом грибе пока нет в справочнике.");
            }

            return result;

        } else {
            // Ошибка Python-сервиса
            InputStream errorStream = connection.getErrorStream();
            String errorMsg = "No details";
            if (errorStream != null) {
                errorMsg = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            throw new RuntimeException("Python Service Error (" + responseCode + "): " + errorMsg);
        }
    }

    // DTO для ответа (можно вынести в отдельный файл, но так удобнее)
    @Data
    public static class RecognitionResult {
        private String aiKey;        // Ключ от нейросети (penny_bun)
        private double confidence;   // Точность (99.9)
        private boolean found;       // Нашли ли в БД?
        private String displayName;  // Русское название
        private String description;  // Описание
        private Boolean edible;      // Съедобен?
        private String cookingTips;  // Советы
    }
}