package org.example.api;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.example.model.Ad;
import org.example.model.Category;
import org.example.model.MessageDTO;
import org.example.model.RecognitionResult;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

public class ApiClient {

    private static final String SERVER_URL = "http://localhost:8080/api";
    private final HttpClient client = HttpClient.newHttpClient();


    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonContext) ->
                    LocalDateTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, type, jsonContext) ->
                    new JsonPrimitive(src.toString()))
            .create();

    // --- AUTH ---
    public String register(String u, String p) throws Exception { return sendAuth(u, p, "/auth/register"); }
    public String login(String u, String p) throws Exception { return sendAuth(u, p, "/auth/login"); }

    private String sendAuth(String u, String p, String endpoint) throws Exception {
        Map<String, String> map = new HashMap<>(); map.put("username", u); map.put("password", p);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(map))).build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new RuntimeException(resp.body());
        return resp.body();
    }

    // Получить данные пользователя (себя или чужого)
    public org.example.model.User getUserProfile(String username) throws Exception {
        // Кодируем URL, чтобы русские имена не ломали запрос
        String encodedName = URLEncoder.encode(username, "UTF-8");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/users/" + encodedName))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) return null;
        return gson.fromJson(resp.body(), org.example.model.User.class);
    }

    // Обновить свой профиль
    public void updateProfile(String username, org.example.model.User userData) throws Exception {
        String encodedName = URLEncoder.encode(username, "UTF-8");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/users/profile?username=" + encodedName))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(userData)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new RuntimeException(resp.body());
    }

    // --- ADS ---
    public List<Category> getCategories() throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/ads/categories")).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(resp.body(), new TypeToken<List<Category>>(){}.getType());
    }

    public List<Ad> getAds(String query, Integer categoryId) throws Exception {
        StringBuilder url = new StringBuilder(SERVER_URL + "/ads?temp=1");
        if (query != null && !query.isEmpty()) url.append("&query=").append(URLEncoder.encode(query, "UTF-8"));
        if (categoryId != null && categoryId > 0) url.append("&categoryId=").append(categoryId);

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url.toString())).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            System.err.println("SERVER ERROR: " + resp.body());
            return new ArrayList<>();
        }
        return gson.fromJson(resp.body(), new TypeToken<List<Ad>>(){}.getType());
    }

    public void deleteAd(Integer id, String username) throws Exception {
        String url = SERVER_URL + "/ads/" + id + "?username=" + username;
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build();
        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    public void createAd(String username, Category category, String title, String desc, BigDecimal price, File imageFile) throws Exception {
        String boundary = Long.toHexString(System.currentTimeMillis());
        String CRLF = "\r\n";
        URL url = new URL(SERVER_URL + "/ads");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true); conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream output = conn.getOutputStream(); PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {
            addFormField(writer, boundary, "username", username);
            addFormField(writer, boundary, "categoryId", String.valueOf(category.getId()));
            addFormField(writer, boundary, "title", title);
            addFormField(writer, boundary, "description", desc);
            addFormField(writer, boundary, "price", price.toString());

            if (imageFile != null) {
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + imageFile.getName() + "\"").append(CRLF);
                writer.append("Content-Type: image/jpeg").append(CRLF);
                writer.append(CRLF).flush();
                Files.copy(imageFile.toPath(), output);
                output.flush();
                writer.append(CRLF).flush();
            }
            writer.append("--" + boundary + "--").append(CRLF).flush();
        }
        if (conn.getResponseCode() != 200) throw new RuntimeException("Error: " + conn.getResponseCode());
    }

    private void addFormField(PrintWriter writer, String boundary, String name, String value) {
        writer.append("--" + boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append("\r\n");
        writer.append("Content-Type: text/plain; charset=UTF-8").append("\r\n");
        writer.append("\r\n");
        writer.append(value).append("\r\n");
        writer.flush();
    }

    // --- CHAT & AI ---
    public void sendMessage(String s, String r, String t) throws Exception {
        Map<String, String> d = new HashMap<>(); d.put("senderName", s); d.put("receiverName", r); d.put("text", t);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/chat/send")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gson.toJson(d))).build();
        client.send(req, HttpResponse.BodyHandlers.ofString());
    }
    public List<String> getChatPartners(String myName) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/chat/partners?myName=" + myName)).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if(resp.statusCode()!=200) return new ArrayList<>();
        return gson.fromJson(resp.body(), new TypeToken<List<String>>(){}.getType());
    }
    public List<MessageDTO> getChatHistory(String u1, String u2) throws Exception {
        String u = String.format(SERVER_URL + "/chat/history?user1=%s&user2=%s", u1, u2);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(u)).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if(resp.statusCode()!=200) return new ArrayList<>();
        return gson.fromJson(resp.body(), new TypeToken<List<MessageDTO>>(){}.getType());
    }
    public RecognitionResult recognizeImage(File f) throws Exception {
        String boundary = Long.toHexString(System.currentTimeMillis());
        String CRLF = "\r\n";

        // ПРОВЕРЬ URL:
        URL url = new URL(SERVER_URL + "/recognition");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true); conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream output = conn.getOutputStream(); PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {
            writer.append("--" + boundary).append(CRLF);

            // ПРОВЕРЬ ИМЯ ПОЛЯ: name="image" (должно совпадать с Controller)
            writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + f.getName() + "\"").append(CRLF);

            writer.append("Content-Type: image/jpeg").append(CRLF);
            writer.append(CRLF).flush();
            Files.copy(f.toPath(), output);
            output.flush();
            writer.append(CRLF).flush();
            writer.append("--" + boundary + "--").append(CRLF).flush();
        }
        int code = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) response.append(inputLine);
        in.close();

        if (code != 200) throw new RuntimeException("Error: " + response.toString());

        // ПРЕВРАЩАЕМ JSON В ОБЪЕКТ
        return gson.fromJson(response.toString(), (Type) RecognitionResult.class);
    }
}