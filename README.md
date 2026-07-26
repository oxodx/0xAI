<div align="center">
  <h1>[ OxAi ]</h1>
</div>

<p align="center">
  <b>AI chat companion using Ollama.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/github/last-commit/oxodx/oxai?&style=for-the-badge&color=8ad7eb&logo=git&logoColor=D9E0EE&labelColor=1E202B" />
  <img src="https://img.shields.io/github/stars/oxodx/oxai?&style=for-the-badge&color=8ad7eb&logo=git&logoColor=D9E0EE&labelColor=1E202B" />
  <img src="https://img.shields.io/github/repo-size/oxodx/oxai?&style=for-the-badge&color=8ad7eb&logo=git&logoColor=D9E0EE&labelColor=1E202B" />
</p>

---

## Support

- Mod version: `0.1.6`
- Minecraft: `26.2`
- Fabric Loader: `0.17.3+`
- Fabric API: required
- Java: `25+`
- Mod Menu: optional, supported

Mod Menu adds an in-game config screen for the same settings stored in `config/oxai.json`.

## Install

1. Install Fabric Loader and Fabric API for Minecraft `26.1.x`.
2. Put the 0xAI JAR in your `mods` folder.
3. Optional: install [Mod Menu](https://modrinth.com/mod/modmenu) to configure 0xAI in-game.
4. Start Ollama:

```sh
ollama pull llama3.2
ollama serve
```

5. Launch Minecraft.

## Usage

In chat:

```text
@ai how do i find diamonds
@ai what's the best food
@ai how do i make a nether portal
```

The default trigger is `@ai`. You can change it in Mod Menu or in `config/oxai.json`.

## Configuration

With Mod Menu installed:

1. Open Mods.
2. Select 0xAI.
3. Open the config screen.
4. Change settings and press Done.

Without Mod Menu, edit `config/oxai.json`:

```json
{
  "enabled": true,
  "trigger": "@ai",
  "ollamaUrl": "http://localhost:11434",
  "model": "llama3.2:3b",
  "systemPrompt": "You are a helpful AI assistant in a Minecraft server. Keep responses concise and friendly.",
  "maxTokens": 150,
  "temperature": 0.7,
  "responsePrefix": "[AI] ",
  "maxMessageLength": 256,
  "showErrors": true,
  "requestTimeout": 60000,
  "contextMessages": 5
}
```

| Option | What it does |
| --- | --- |
| `enabled` | Turns AI replies on or off |
| `trigger` | Chat text that triggers the AI |
| `ollamaUrl` | Ollama server address |
| `model` | Ollama model name |
| `systemPrompt` | System message sent with each request |
| `maxTokens` | Maximum generated response length |
| `temperature` | Response randomness from `0.0` to `1.0` |
| `responsePrefix` | Prefix added before AI chat messages |
| `maxMessageLength` | Maximum characters per chat line |
| `showErrors` | Shows request errors in chat |
| `requestTimeout` | Ollama request timeout in milliseconds |
| `contextMessages` | Number of recent chat turns to keep as context |

## Models

0xAI works with any Ollama chat model. Common choices:

- `llama3.2:3b`
- `llama3.2:1b`
- `mistral`
- `phi3`

Use smaller models for faster replies or lower-end hardware.

## Build

Install JDK `25+`, then build with Gradle:

```sh
./gradlew build
```

On Windows PowerShell, if the Unix wrapper script is not executable, run:

```powershell
java -classpath gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain build
```

The built JAR is written to `build/libs/`.

## Troubleshooting

**Build says `release version 25 not supported`**

Gradle is using an older JDK. Install JDK 25 and make sure `JAVA_HOME` or your IDE Gradle JVM points to it.

**The mod does not respond**

- Make sure 0xAI is enabled in Mod Menu or `config/oxai.json`.
- Check that Ollama is running with `ollama serve`.
- Check that the configured model is installed with `ollama list`.
- Verify that your message contains the configured trigger.

**Replies are slow**

- Use a smaller model such as `llama3.2:1b`.
- Lower `maxTokens`.
- Lower `contextMessages`.

**Connection refused**

- Check `ollamaUrl`.
- Make sure Ollama is running on the configured host and port.

---

<p align="center">
  <a href="https://github.com/oxodx/oxai/graphs/contributors">Contributors</a> &bull;
  <a href="LICENSE">GPL-3 License</a>
</p>
