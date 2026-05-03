package nl.oxod.oxai;

import com.google.gson.Gson;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class OllamaClient {
  private final Gson gson = new Gson();
  private final List<Message> conversationHistory = new ArrayList<>();
  private final OxAIConfig config;

  public OllamaClient(OxAIConfig config) {
    this.config = config;
  }

  public static class Message {
    private String role;
    private String content;

    public Message() {
    }

    public Message(String role, String content) {
      this.role = role;
      this.content = content;
    }

    public String getRole() {
      return role;
    }

    public void setRole(String role) {
      this.role = role;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }
  }

  public static class OllamaRequest {
    private String model;
    private List<Message> messages;
    private boolean stream;
    private Options options;

    public OllamaRequest() {
    }

    public OllamaRequest(String model, List<Message> messages, boolean stream, Options options) {
      this.model = model;
      this.messages = messages;
      this.stream = stream;
      this.options = options;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }

    public List<Message> getMessages() {
      return messages;
    }

    public void setMessages(List<Message> messages) {
      this.messages = messages;
    }

    public boolean isStream() {
      return stream;
    }

    public void setStream(boolean stream) {
      this.stream = stream;
    }

    public Options getOptions() {
      return options;
    }

    public void setOptions(Options options) {
      this.options = options;
    }
  }

  public static class Options {
    private double temperature;
    private int num_predict;

    public Options() {
    }

    public Options(double temperature, int num_predict) {
      this.temperature = temperature;
      this.num_predict = num_predict;
    }

    public double getTemperature() {
      return temperature;
    }

    public void setTemperature(double temperature) {
      this.temperature = temperature;
    }

    public int getNum_predict() {
      return num_predict;
    }

    public void setNum_predict(int num_predict) {
      this.num_predict = num_predict;
    }
  }

  public static class OllamaResponse {
    private Message message;
    private boolean done;

    public Message getMessage() {
      return message;
    }

    public void setMessage(Message message) {
      this.message = message;
    }

    public boolean isDone() {
      return done;
    }

    public void setDone(boolean done) {
      this.done = done;
    }
  }

  public String generateResponse(String prompt) {
    conversationHistory.add(new Message("user", prompt));

    if (conversationHistory.size() > config.getContextMessages() * 2) {
      conversationHistory.remove(0);
      conversationHistory.remove(0);
    }

    List<Message> messages = new ArrayList<>();
    messages.add(new Message("system", config.getSystemPrompt()));
    messages.addAll(conversationHistory);

    OllamaRequest request = new OllamaRequest(
        config.getModel(),
        messages,
        false,
        new Options(config.getTemperature(), config.getMaxTokens()));

    String requestJson = gson.toJson(request);

    HttpURLConnection connection = null;
    try {
      URL url = new URL(config.getOllamaUrl() + "/api/chat");
      connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);
      connection.setConnectTimeout(config.getRequestTimeout());
      connection.setReadTimeout(config.getRequestTimeout());

      try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
        writer.write(requestJson);
        writer.flush();
      }

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        String response;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
          StringBuilder sb = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
            sb.append(line);
          }
          response = sb.toString();
        }

        OllamaResponse ollamaResponse = gson.fromJson(response, OllamaResponse.class);
        String assistantMessage = ollamaResponse.getMessage().getContent();

        conversationHistory.add(new Message("assistant", assistantMessage));

        return assistantMessage;
      } else {
        String errorMessage;
        if (connection.getErrorStream() != null) {
          try (BufferedReader reader = new BufferedReader(
              new InputStreamReader(connection.getErrorStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
              sb.append(line);
            }
            errorMessage = sb.toString();
          }
        } else {
          errorMessage = "HTTP " + responseCode;
        }
        throw new RuntimeException("Ollama request failed: " + errorMessage);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  public void clearHistory() {
    conversationHistory.clear();
  }
}
