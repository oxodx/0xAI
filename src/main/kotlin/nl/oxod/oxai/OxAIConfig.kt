package nl.oxod.oxai

import com.google.gson.GsonBuilder
import java.nio.file.Files
import java.nio.file.Paths

data class OxAIConfig(
  var enabled: Boolean = true,
  var trigger: String = "@ai",
  var ollamaUrl: String = "http://localhost:11434",
  var model: String = "llama3.2:3b",
  var systemPrompt: String = "You are a helpful AI assistant in a Minecraft server. Keep responses concise and friendly.",
  var maxTokens: Int = 150,
  var temperature: Double = 0.7,
  var responsePrefix: String = "[0xAI] ",
  var maxMessageLength: Int = 256,
  var showErrors: Boolean = true,
  var requestTimeout: Int = 30000,
  var contextMessages: Int = 5
) {
  companion object {
    private val configPath = Paths.get("config", "${OxAI.MODID}.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun load(): OxAIConfig {
      val configFile = configPath.toFile()

      return if (configFile.exists()) {
        try {
          val json = Files.readString(configPath)
          gson.fromJson(json, OxAIConfig::class.java).also {
            OxAI.logger.info("Loaded config from ${configFile.absolutePath}")
          }
        } catch (e: Exception) {
          OxAI.logger.error("Failed to load config, using defaults", e)
          OxAIConfig().also { it.save() }
        }
      } else {
        OxAI.logger.info("Config file not found, creating default")
        OxAIConfig().also { it.save() }
      }
    }
  }

  fun save() {
    try {
      val configFile = configPath.toFile()
      configFile.parentFile.mkdirs()

      val json = gson.toJson(this)
      Files.writeString(configPath, json)

      OxAI.logger.info("Saved config to ${configFile.absolutePath}")
    } catch (e: Exception) {
      OxAI.logger.error("Failed to save config", e)
    }
  }
}
