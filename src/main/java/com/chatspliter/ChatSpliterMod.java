package com.chatspliter;

import com.chatspliter.config.ChatSpliterConfig;
import com.chatspliter.hud.ChatHudManager;
import com.chatspliter.screen.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.net.URI;
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

    public static void onClientTick(MinecraftClient client) {
        if (client.player == null) return;
        while (OPEN_CONFIG_KEY.wasPressed()) {
            client.setScreen(new ConfigScreen(null));
        }
    }

    /** Inject a client-side test message with click and hover events. */
    public static void injectDebugMessage(MinecraftClient client) {
        try {
        Text msg = Text.empty()
                .append(Text.literal("[ChatSpliter Debug] ").formatted(Formatting.GOLD))
                .append(Text.literal("[GitHub]").setStyle(Style.EMPTY
                        .withClickEvent(createOpenUrl("https://github.com"))
                        .withColor(Formatting.AQUA)))
                .append(Text.literal(" "))
                .append(Text.literal("[Say Hi]").setStyle(Style.EMPTY
                        .withClickEvent(createSuggestCommand("hi"))
                        .withColor(Formatting.GREEN)))
                .append(Text.literal(" "))
                .append(Text.literal("[Hover]").setStyle(Style.EMPTY
                        .withHoverEvent(createShowText(Text.literal("悬停测试成功！").formatted(Formatting.YELLOW)))
                        .withColor(Formatting.YELLOW)));

        ChatHudManager.getInstance().onChatMessage(msg, (int) (System.currentTimeMillis() / 50));

        if (client.player != null) {
            client.player.sendMessage(msg, false);
        }

        LOGGER.info("[ChatSpliter] Debug test message injected.");
        } catch (Throwable e) { LOGGER.warn("[ChatSpliter] Debug message failed: {}", e.toString()); }
    }

    private static ClickEvent createOpenUrl(String url) {
        try { return new ClickEvent.OpenUrl(java.net.URI.create(url)); } catch (Throwable e) { return null; }
    }
    private static ClickEvent createSuggestCommand(String cmd) {
        try { return new ClickEvent.SuggestCommand(cmd); } catch (Throwable e) { return null; }
    }
    private static HoverEvent createShowText(Text text) {
        try { return new HoverEvent.ShowText(text); } catch (Throwable e) { return null; }
    }
}
