package com.adityaburi999.cc.client;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final AutoCrystalConfig config;

    public ConfigScreen(Screen parent, AutoCrystalConfig config) {
        super(Text.translatable("screen.cc.title"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int y = height / 4;

        addDrawableChild(ButtonWidget.builder(enabledText(), button -> {
            config.enabled = !config.enabled;
            button.setMessage(enabledText());
        }).dimensions(centerX - 100, y, 200, 20).build());

        y += 24;
        addDrawableChild(createSlider(centerX - 100, y, AutoCrystalConfig.MAX_CPS_MIN, AutoCrystalConfig.MAX_CPS_MAX,
            () -> config.maxCps,
            value -> config.maxCps = value,
            value -> Text.translatable("option.cc.max_cps", formatNumber(value))
        ));

        y += 24;
        addDrawableChild(createSlider(centerX - 100, y, AutoCrystalConfig.MIN_FACTOR_MIN, AutoCrystalConfig.MIN_FACTOR_MAX,
            () -> config.minCpsFactor,
            value -> config.minCpsFactor = value,
            value -> Text.translatable("option.cc.min_cps_factor", formatNumber(value))
        ));

        y += 24;
        addDrawableChild(createSlider(centerX - 100, y, AutoCrystalConfig.MISCLICK_MIN, AutoCrystalConfig.MISCLICK_MAX,
            () -> config.misclickChance,
            value -> config.misclickChance = value,
            value -> Text.translatable("option.cc.misclick_chance", formatPercent(value))
        ));

        y += 32;
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.cc.done"), button -> close())
            .dimensions(centerX - 100, y, 200, 20)
            .build());
    }

    @Override
    public void close() {
        config.save();
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private Text enabledText() {
        return Text.translatable("option.cc.enabled", config.enabled ? "On" : "Off");
    }

    private SliderWidget createSlider(int x, int y, double min, double max, DoubleSupplier getter,
                                      DoubleConsumer setter, Function<Double, Text> messageFactory) {
        double initial = (getter.getAsDouble() - min) / (max - min);
        return new SliderWidget(x, y, 200, 20, Text.empty(), initial) {
            @Override
            protected void updateMessage() {
                setMessage(messageFactory.apply(getValue()));
            }

            @Override
            protected void applyValue() {
                setter.accept(getValue());
            }

            private double getValue() {
                double raw = min + (value * (max - min));
                return Math.max(min, Math.min(max, raw));
            }
        };
    }

    private String formatNumber(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatPercent(double value) {
        return String.format(Locale.US, "%.0f%%", value * 100.0);
    }
}
