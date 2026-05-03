package nl.oxod.oxai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;

public class OxAIConfig {
  private static final java.nio.file.Path configPath = Paths.get("config", OxAI.MODID + ".json");
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private boolean enabled = true;
  private String trigger = "@ai";
  private String ollamaUrl = "http://localhost:11434";
  private String model = "llama3.2:3b";
  private String systemPrompt = "You are a helpful AI assistant in a Minecraft server. Keep responses concise and friendly.";
  private int maxTokens = 150;
  private double temperature = 0.7;
  private String responsePrefix = "[AI] ";
  private int maxMessageLength = 256;
  private boolean showErrors = true;
  private int requestTimeout = 60000;
  private int contextMessages = 5;

  public static OxAIConfig load() {
    File configFile = configPath.toFile();

    if (configFile.exists()) {
      try {
        String json = Files.readString(configPath);
        OxAIConfig config = gson.fromJson(json, OxAIConfig.class);
        OxAI.getLogger().info("Loaded config from " + configFile.getAbsolutePath());
        return config;
      } catch (Exception e) {
        OxAI.getLogger().error("Failed to load config, using defaults", e);
        OxAIConfig config = new OxAIConfig();
        config.save();
        return config;
      }
    } else {
      OxAI.getLogger().info("Config file not found, creating default");
      OxAIConfig config = new OxAIConfig();
      config.save();
      return config;
    }
  }

  public void save() {
    try {
      File configFile = configPath.toFile();
      configFile.getParentFile().mkdirs();

      String json = gson.toJson(this);
      Files.writeString(configPath, json);

      OxAI.getLogger().info("Saved config to " + configFile.getAbsolutePath());
    } catch (Exception e) {
      OxAI.getLogger().error("Failed to save config", e);
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getTrigger() {
    return trigger;
  }

  public void setTrigger(String trigger) {
    this.trigger = trigger;
  }

  public String getOllamaUrl() {
    return ollamaUrl;
  }

  public void setOllamaUrl(String ollamaUrl) {
    this.ollamaUrl = ollamaUrl;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getSystemPrompt() {
    return systemPrompt;
  }

  public void setSystemPrompt(String systemPrompt) {
    this.systemPrompt = systemPrompt;
  }

  public int getMaxTokens() {
    return maxTokens;
  }

  public void setMaxTokens(int maxTokens) {
    this.maxTokens = maxTokens;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public String getResponsePrefix() {
    return responsePrefix;
  }

  public void setResponsePrefix(String responsePrefix) {
    this.responsePrefix = responsePrefix;
  }

  public int getMaxMessageLength() {
    return maxMessageLength;
  }

  public void setMaxMessageLength(int maxMessageLength) {
    this.maxMessageLength = maxMessageLength;
  }

  public boolean isShowErrors() {
    return showErrors;
  }

  public void setShowErrors(boolean showErrors) {
    this.showErrors = showErrors;
  }

  public int getRequestTimeout() {
    return requestTimeout;
  }

  public void setRequestTimeout(int requestTimeout) {
    this.requestTimeout = requestTimeout;
  }

  public int getContextMessages() {
    return contextMessages;
  }

  public void setContextMessages(int contextMessages) {
    this.contextMessages = contextMessages;
  }
}
