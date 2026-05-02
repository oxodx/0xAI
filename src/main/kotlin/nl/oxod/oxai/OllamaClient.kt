package nl.oxod.oxai

import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class OllamaClient(private val config: OxAIConfig) {
  private val gson = Gson()
  private val conversationHistory = mutableListOf<Message>()

  data class Message(
    val role: String,
    val content: String
  )

  data class OllamaRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = false,
    val options: Options? = null
  )

  data class Options(
    val temperature: Double,
    val num_predict: Int
  )

  data class OllamaResponse(
    val message: Message,
    val done: Boolean
  )

  fun generateResponse(prompt: String): String {
    conversationHistory.add(Message("user", prompt))

    if (conversationHistory.size > config.contextMessages * 2) {
      conversationHistory.removeAt(0)
      conversationHistory.removeAt(0)
    }

    val messages = mutableListOf<Message>()
    messages.add(Message("system", config.systemPrompt))
    messages.addAll(conversationHistory)

    val request = OllamaRequest(
      model = config.model,
      messages = messages,
      stream = false,
      options = Options(
        temperature = config.temperature,
        num_predict = config.maxTokens
      )
    )

    val requestJson = gson.toJson(request)

    val url = URL("${config.ollamaUrl}/api/chat")
    val connection = url.openConnection() as HttpURLConnection

    try {
      connection.requestMethod = "POST"
      connection.setRequestProperty("Content-Type", "application/json")
      connection.doOutput = true
      connection.connectTimeout = config.requestTimeout
      connection.readTimeout = config.requestTimeout

      OutputStreamWriter(connection.outputStream).use { writer ->
        writer.write(requestJson)
        writer.flush()
      }

      val responseCode = connection.responseCode
      if (responseCode == HttpURLConnection.HTTP_OK) {
        val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
          reader.readText()
        }

        val ollamaResponse = gson.fromJson(response, OllamaResponse::class.java)
        val assistantMessage = ollamaResponse.message.content

        conversationHistory.add(Message("assistant", assistantMessage))

        return assistantMessage
      } else {
        val errorStream = connection.errorStream
        val errorMessage = if (errorStream != null) {
          BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
        } else {
          "HTTP $responseCode"
        }
        throw Exception("Ollama request failed: $errorMessage")
      }
    } finally {
      connection.disconnect()
    }
  }

  fun clearHistory() {
    conversationHistory.clear()
  }
}
