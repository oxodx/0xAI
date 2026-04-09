package dev.oxod.oxai

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory

object OxAI : ClientModInitializer {
  const val MODID = "oxai"
  val logger by lazy { LoggerFactory.getLogger(MODID) }

  lateinit var config: OxAIConfig
  lateinit var ollamaClient: OllamaClient

  private val messageQueue by lazy { mutableListOf<String>() }
  private var tickCounter = 0

  override fun onInitializeClient() {
    logger.info("Initializing 0xAI...")

    config = OxAIConfig.load()
    ollamaClient = OllamaClient(config)

    ClientReceiveMessageEvents.CHAT.register(ClientReceiveMessageEvents.Chat { message, _, _, _, _ ->
      val messageText = message.string
      logger.info(messageText)
      if (messageText.contains(config.trigger, ignoreCase = true)) {
        handleAIRequest(messageText)
      }
    })

    ClientTickEvents.END_CLIENT_TICK.register { client ->
      tickCounter++
      if (tickCounter >= 20 && messageQueue.isNotEmpty()) {
        tickCounter = 0
        val msg = sanitizeChatMessage(messageQueue.removeAt(0))
        sendChatMessage(client, msg)
      }
    }

    logger.info("0xAI initialized successfully!")
  }

  private fun handleAIRequest(message: String) {
    if (!config.enabled) {
      logger.info("OxAI is disabled in config")
      return
    }

    val question = message.replace(config.trigger, "", ignoreCase = true).trim()
    if (question.isEmpty()) {
      logger.info("No question found, message only contained trigger")
      return
    }

    Thread {
      try {
        logger.info("Sending request to Ollama: $question")
        val response = ollamaClient.generateResponse(message)
        splitMessage(response).forEach { chunk ->
          messageQueue.add(chunk)
        }
      } catch (e: Exception) {
        logger.error("Failed to get response from Ollama", e)
        if (config.showErrors) {
          messageQueue.add("${config.responsePrefix}Error: ${e.message}")
        }
      }
    }.start()
  }

  private fun splitMessage(message: String): List<String> {
    val maxLength = config.maxMessageLength
    val prefix = config.responsePrefix

    if (message.length + prefix.length <= maxLength) return listOf("$prefix$message")

    val chunks = mutableListOf<String>()
    var remaining = message
    while (remaining.isNotEmpty()) {
      val availableLength = maxLength - prefix.length
      val splitIndex = remaining.lastIndexOf(' ', availableLength).takeIf { it != -1 } ?: availableLength
      chunks.add("$prefix${remaining.substring(0, splitIndex)}")
      remaining = remaining.substring(splitIndex).trim()
    }
    return chunks
  }

  private fun sendChatMessage(client: Minecraft, message: String) {
    client.player?.connection?.sendChat(message)
  }

  private fun sanitizeChatMessage(msg: String): String {
    return msg.filter { it.code in 32..126 }
  }
}
