package nl.oxod.oxai;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class OxAI implements ClientModInitializer {
  public static final String MODID = "oxai";
  private static final Logger logger = LoggerFactory.getLogger(MODID);

  private OxAIConfig config;
  private OllamaClient ollamaClient;

  private final List<String> messageQueue = new ArrayList<>();
  private int tickCounter = 0;

  @Override
  public void onInitializeClient() {
    logger.info("Initializing 0xAI...");

    config = OxAIConfig.load();
    ollamaClient = new OllamaClient(config);

    ClientReceiveMessageEvents.CHAT.register((message, signedMessage, messageType, senderUuid, params) -> {
      String messageText = message.getString();
      logger.info(messageText);
      if (messageText.toLowerCase().contains(config.getTrigger().toLowerCase())) {
        handleAIRequest(messageText);
      }
    });

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      tickCounter++;
      if (tickCounter >= 20 && !messageQueue.isEmpty()) {
        tickCounter = 0;
        String msg = sanitizeChatMessage(messageQueue.remove(0));
        sendChatMessage(client, msg);
      }
    });

    logger.info("0xAI initialized successfully!");
  }

  private void handleAIRequest(String message) {
    if (!config.isEnabled()) {
      logger.info("OxAI is disabled in config");
      return;
    }

    String question = message.replaceAll("(?i)" + config.getTrigger(), "").trim();
    if (question.isEmpty()) {
      logger.info("No question found, message only contained trigger");
      return;
    }

    new Thread(() -> {
      try {
        logger.info("Sending request to Ollama: " + question);
        String response = ollamaClient.generateResponse(message);
        for (String chunk : splitMessage(response)) {
          messageQueue.add(chunk);
        }
      } catch (Exception e) {
        logger.error("Failed to get response from Ollama", e);
        if (config.isShowErrors()) {
          messageQueue.add(config.getResponsePrefix() + "Error: " + e.getMessage());
        }
      }
    }).start();
  }

  private List<String> splitMessage(String message) {
    List<String> chunks = new ArrayList<>();
    int maxLength = config.getMaxMessageLength();
    String prefix = config.getResponsePrefix();

    if (message.length() + prefix.length() <= maxLength) {
      chunks.add(prefix + message);
      return chunks;
    }

    String remaining = message;
    while (!remaining.isEmpty()) {
      int availableLength = maxLength - prefix.length();
      int splitIndex = remaining.lastIndexOf(' ', availableLength);
      if (splitIndex == -1 || splitIndex > availableLength) {
        splitIndex = Math.min(availableLength, remaining.length());
      }
      chunks.add(prefix + remaining.substring(0, splitIndex));
      remaining = remaining.substring(splitIndex).trim();
    }
    return chunks;
  }

  private void sendChatMessage(Minecraft client, String message) {
    if (client.player != null && client.player.connection != null) {
      client.player.connection.sendChat(message);
    }
  }

  private String sanitizeChatMessage(String msg) {
    StringBuilder sb = new StringBuilder();
    for (char c : msg.toCharArray()) {
      if (c >= 32 && c <= 126) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public static Logger getLogger() {
    return logger;
  }
}
