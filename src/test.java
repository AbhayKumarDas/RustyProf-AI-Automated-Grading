import java.io.*;
import java.net.*;
import java.util.stream.Collectors;

public class test {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://router.huggingface.co/nebius/v1/chat/completions";

    public static void main(String[] args) {
        try {
            String response = callChatAPI("What is the capital of Nepal in 2025?");
            String answer = extractAnswerFromResponse(response);
            System.out.println("AI Response: " + answer);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String callChatAPI(String message) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);

        String payload = String.format(
                "{\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]," +
                        "\"model\":\"mistralai/Mistral-Nemo-Instruct-2407-fast\"}",
                message.replace("\"", "\\\"")
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes("UTF-8"));
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            return br.lines().collect(Collectors.joining());
        }
    }

    private static String extractAnswerFromResponse(String jsonResponse) {
        // Look for the "content" field in the JSON
        String searchPattern = "\"content\":\"";
        int start = jsonResponse.indexOf(searchPattern);
        if (start == -1) return "Could not find answer in response";

        start += searchPattern.length();
        int end = jsonResponse.indexOf("\"", start);
        if (end == -1) return "Malformed response";

        return jsonResponse.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
    }
}