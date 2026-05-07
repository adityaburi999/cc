package com.adityaburi999.cc.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AutoCrystalClient implements ClientModInitializer {
    private static AutoCrystalConfig config;
    private final AutoCrystalController controller = new AutoCrystalController();
    private KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        config = AutoCrystalConfig.load();
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.cc.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.cc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.wasPressed()) {
                client.setScreen(new ConfigScreen(client.currentScreen, config));
            }
            controller.tick(client, config);
        });
    }
}
