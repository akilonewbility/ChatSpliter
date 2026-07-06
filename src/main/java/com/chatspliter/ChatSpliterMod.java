package com.chatspliter;

import com.chatspliter.config.ChatSpliterConfig;
import com.chatspliter.hud.ChatHudManager;
import com.chatspliter.screen.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatSpliterMod implements ClientModInitializer {
    public static final String MOD_ID = "chatspliter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final KeyBinding OPEN_CONFIG_KEY = new KeyBinding(
            "chatspliter.key.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.misc"
    );

    @Override
    public void onInitializeClient() {
        LOGGER.info("[ChatSpliter] Initializing...");
        ChatSpliterConfig.getInstance();
        ChatHudManager.getInstance().initialize();
        LOGGER.info("[ChatSpliter] Initialized successfully! Press K to open config.");
    }

    /**
     * Called from MinecraftClientMixin each tick to check key presses.
     */
    public static void onClientTick(MinecraftClient client) {
        if (client.player == null) return;
        while (OPEN_CONFIG_KEY.wasPressed()) {
            client.setScreen(new ConfigScreen(null));
        }
    }
}
