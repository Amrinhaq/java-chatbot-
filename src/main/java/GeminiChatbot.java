package com.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GeminiChatbot {

    private static final String API_KEY ="YOUR-API-KEY";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Gemini Java Chatbot. Type 'exit' to quit.");

        while (true) {
            System.out.print("\nYou: ");
            String prompt = scanner.nextLine();
            if (prompt.equalsIgnoreCase("exit")) break;

            String response = askGemini(prompt);
            System.out.println("Gemini: " + response);
        }
    }

    public static String askGemini(String message) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> content = new HashMap<>();
        content.put("parts", new Object[]{ Map.of("text", message) });

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", new Object[]{ content });

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Raw API Response: " + response.body());

        Map<String, Object> json = mapper.readValue(response.body(), Map.class);

        if (json.containsKey("candidates")) {
            var candidates = (java.util.List<Map<String, Object>>) json.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> firstCandidate = candidates.get(0);
                Map<String, Object> contentMap = (Map<String, Object>) firstCandidate.get("content");
                var parts = (java.util.List<Map<String, Object>>) contentMap.get("parts");
                if (parts != null && !parts.isEmpty() && parts.get(0).containsKey("text")) {
                    return parts.get(0).get("text").toString().trim();
                }
            }
        } else if (json.containsKey("error")) {
            Map<String, Object> error = (Map<String, Object>) json.get("error");
            return "API Error: " + error.get("message");
        }

        return "Error: Unexpected API response.";
    }
}
