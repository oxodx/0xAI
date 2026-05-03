package nl.oxod.oxai;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OxAIConfigScreen extends Screen {
  private static final int LABEL_WIDTH = 120;
  private static final int FIELD_WIDTH = 210;
  private static final int ROW_HEIGHT = 24;

  private final Screen parent;
  private final OxAIConfig config;

  private CycleButton<Boolean> enabled;
  private EditBox trigger;
  private EditBox ollamaUrl;
  private EditBox model;
  private EditBox systemPrompt;
  private EditBox maxTokens;
  private EditBox temperature;
  private EditBox responsePrefix;
  private EditBox maxMessageLength;
  private CycleButton<Boolean> showErrors;
  private EditBox requestTimeout;
  private EditBox contextMessages;
  private StringWidget status;

  public OxAIConfigScreen(Screen parent) {
    super(Component.literal("OxAI Config"));
    this.parent = parent;
    this.config = OxAI.getConfig();
  }

  @Override
  protected void init() {
    int rowWidth = LABEL_WIDTH + FIELD_WIDTH;
    int x = (this.width - rowWidth) / 2;
    int y = 20;

    addRenderableOnly(new StringWidget(0, y, this.width, 20, this.title, this.font));
    y += 28;

    enabled = CycleButton.onOffBuilder(config.isEnabled())
        .create(x + LABEL_WIDTH, y, FIELD_WIDTH, 20, Component.literal("Enabled"));
    addRow(x, y, "Enabled", enabled);
    y += ROW_HEIGHT;

    trigger = addTextRow(x, y, "Trigger", config.getTrigger(), 64);
    y += ROW_HEIGHT;
    ollamaUrl = addTextRow(x, y, "Ollama URL", config.getOllamaUrl(), 256);
    y += ROW_HEIGHT;
    model = addTextRow(x, y, "Model", config.getModel(), 128);
    y += ROW_HEIGHT;
    systemPrompt = addTextRow(x, y, "System Prompt", config.getSystemPrompt(), 512);
    y += ROW_HEIGHT;
    maxTokens = addTextRow(x, y, "Max Tokens", Integer.toString(config.getMaxTokens()), 8);
    y += ROW_HEIGHT;
    temperature = addTextRow(x, y, "Temperature", Double.toString(config.getTemperature()), 8);
    y += ROW_HEIGHT;
    responsePrefix = addTextRow(x, y, "Response Prefix", config.getResponsePrefix(), 64);
    y += ROW_HEIGHT;
    maxMessageLength = addTextRow(x, y, "Max Message Length", Integer.toString(config.getMaxMessageLength()), 8);
    y += ROW_HEIGHT;

    showErrors = CycleButton.onOffBuilder(config.isShowErrors())
        .create(x + LABEL_WIDTH, y, FIELD_WIDTH, 20, Component.literal("Show Errors"));
    addRow(x, y, "Show Errors", showErrors);
    y += ROW_HEIGHT;

    requestTimeout = addTextRow(x, y, "Request Timeout", Integer.toString(config.getRequestTimeout()), 8);
    y += ROW_HEIGHT;
    contextMessages = addTextRow(x, y, "Context Messages", Integer.toString(config.getContextMessages()), 8);
    y += ROW_HEIGHT + 6;

    status = new StringWidget(0, y, this.width, 20, Component.empty(), this.font);
    addRenderableOnly(status);
    y += 26;

    int buttonX = (this.width - 206) / 2;
    addRenderableWidget(Button.builder(Component.literal("Save"), button -> save())
        .bounds(buttonX, y, 100, 20)
        .build());
    addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> close())
        .bounds(buttonX + 106, y, 100, 20)
        .build());
  }

  @Override
  public void onClose() {
    close();
  }

  private EditBox addTextRow(int x, int y, String label, String value, int maxLength) {
    EditBox box = new EditBox(this.font, x + LABEL_WIDTH, y, FIELD_WIDTH, 20, Component.literal(label));
    box.setMaxLength(maxLength);
    box.setValue(value);
    addRow(x, y, label, box);
    return box;
  }

  private void addRow(int x, int y, String label, net.minecraft.client.gui.components.AbstractWidget widget) {
    addRenderableOnly(new StringWidget(x, y + 5, LABEL_WIDTH - 8, 10, Component.literal(label), this.font));
    addRenderableWidget(widget);
  }

  private void save() {
    try {
      config.setEnabled(enabled.getValue());
      config.setTrigger(trigger.getValue());
      config.setOllamaUrl(ollamaUrl.getValue());
      config.setModel(model.getValue());
      config.setSystemPrompt(systemPrompt.getValue());
      config.setMaxTokens(parseInt(maxTokens, "Max Tokens"));
      config.setTemperature(parseDouble(temperature, "Temperature"));
      config.setResponsePrefix(responsePrefix.getValue());
      config.setMaxMessageLength(parseInt(maxMessageLength, "Max Message Length"));
      config.setShowErrors(showErrors.getValue());
      config.setRequestTimeout(parseInt(requestTimeout, "Request Timeout"));
      config.setContextMessages(parseInt(contextMessages, "Context Messages"));
      config.save();
      status.setMessage(Component.literal("Saved"));
    } catch (IllegalArgumentException e) {
      status.setMessage(Component.literal(e.getMessage()));
    }
  }

  private int parseInt(EditBox box, String label) {
    try {
      return Integer.parseInt(box.getValue());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(label + " must be a whole number");
    }
  }

  private double parseDouble(EditBox box, String label) {
    try {
      return Double.parseDouble(box.getValue());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(label + " must be a number");
    }
  }

  private void close() {
    if (this.minecraft != null) {
      this.minecraft.setScreen(parent);
    }
  }
}
