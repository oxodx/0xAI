package nl.oxod.oxai;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class OxAIConfigScreen extends OptionsSubScreen {
  private final OxAIConfig config;

  public OxAIConfigScreen(Screen parent) {
    super(parent, Minecraft.getInstance().options, Component.translatable("oxai.options.title"));
    this.config = OxAI.getConfig();
  }

  @Override
  protected void addOptions() {
    if (this.list == null) {
      return;
    }

    this.list.addSmall(
        OptionInstance.createBoolean("oxai.options.enabled", config.isEnabled(), config::setEnabled),
        OptionInstance.createBoolean("oxai.options.show_errors", config.isShowErrors(), config::setShowErrors));

    this.list.addSmall(
        intOption("oxai.options.max_tokens", 1, 2048, config.getMaxTokens(), config::setMaxTokens),
        doubleOption("oxai.options.temperature", config.getTemperature(), config::setTemperature));

    this.list.addSmall(
        intOption("oxai.options.max_message_length", 64, 256, config.getMaxMessageLength(),
            config::setMaxMessageLength),
        intOption("oxai.options.request_timeout", 1000, 120000, config.getRequestTimeout(),
            config::setRequestTimeout));

    this.list.addSmall(
        intOption("oxai.options.context_messages", 0, 20, config.getContextMessages(), config::setContextMessages)
            .createButton(this.options),
        stringButton("oxai.options.trigger", config::getTrigger, config::setTrigger, 64));

    List<AbstractWidget> stringOptions = new ArrayList<>();
    stringOptions.add(stringButton("oxai.options.ollama_url", config::getOllamaUrl, config::setOllamaUrl, 256));
    stringOptions.add(stringButton("oxai.options.model", config::getModel, config::setModel, 128));
    stringOptions.add(stringButton("oxai.options.response_prefix", config::getResponsePrefix, config::setResponsePrefix,
        64));
    stringOptions.add(stringButton("oxai.options.system_prompt", config::getSystemPrompt, config::setSystemPrompt, 512));
    this.list.addSmall(stringOptions);
  }

  @Override
  public void removed() {
    config.save();
  }

  private OptionInstance<Integer> intOption(String key, int min, int max, int value, Consumer<Integer> setter) {
    return new OptionInstance<>(
        key,
        OptionInstance.noTooltip(),
        this::valueText,
        new OptionInstance.IntRange(min, max),
        value,
        setter);
  }

  private OptionInstance<Double> doubleOption(String key, double value, Consumer<Double> setter) {
    return new OptionInstance<>(
        key,
        OptionInstance.noTooltip(),
        (caption, optionValue) -> valueText(caption, Math.round(optionValue * 100.0) / 100.0),
        OptionInstance.UnitDouble.INSTANCE,
        Codec.doubleRange(0.0, 1.0),
        value,
        setter);
  }

  private Component valueText(Component caption, Object value) {
    return Component.literal(caption.getString() + ": " + value);
  }

  private Button stringButton(String key, Supplier<String> getter, Consumer<String> setter, int maxLength) {
    return Button.builder(valueText(Component.translatable(key), getter.get()), button -> {
      this.minecraft.setScreen(new OxAITextOptionScreen(this, key, getter.get(), maxLength, value -> {
        setter.accept(value);
        button.setMessage(valueText(Component.translatable(key), value));
      }));
    }).build();
  }

  private static class OxAITextOptionScreen extends Screen {
    private final Screen parent;
    private final String key;
    private final String initialValue;
    private final int maxLength;
    private final Consumer<String> onSave;
    private EditBox editBox;

    OxAITextOptionScreen(Screen parent, String key, String initialValue, int maxLength, Consumer<String> onSave) {
      super(Component.translatable(key));
      this.parent = parent;
      this.key = key;
      this.initialValue = initialValue;
      this.maxLength = maxLength;
      this.onSave = onSave;
    }

    @Override
    protected void init() {
      int fieldWidth = Math.min(360, this.width - 40);
      int fieldX = (this.width - fieldWidth) / 2;
      int y = this.height / 2 - 30;

      this.editBox = new EditBox(this.font, fieldX, y, fieldWidth, 20, Component.translatable(key));
      this.editBox.setMaxLength(maxLength);
      this.editBox.setValue(initialValue);
      this.addRenderableWidget(editBox);
      this.setInitialFocus(editBox);

      int buttonX = (this.width - 204) / 2;
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> saveAndClose())
          .bounds(buttonX, y + 32, 100, 20)
          .build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> close())
          .bounds(buttonX + 104, y + 32, 100, 20)
          .build());
    }

    @Override
    public void onClose() {
      close();
    }

    private void saveAndClose() {
      onSave.accept(editBox.getValue());
      close();
    }

    private void close() {
      if (this.minecraft != null) {
        this.minecraft.setScreen(parent);
      }
    }
  }
}
