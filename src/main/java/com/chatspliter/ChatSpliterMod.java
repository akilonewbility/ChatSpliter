package com.chatspliter;

import com.chatspliter.config.ChatSpliterConfig;
import com.chatspliter.hud.ChatHudManager;
import com.chatspliter.screen.ConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ChatSpliterMod.MOD_ID)
public class ChatSpliterMod {
    public static final String MOD_ID = "chatspliter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            "chatspliter.key.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.misc"
    );

    public ChatSpliterMod(IEventBus modEventBus, Dist dist) {
        if (dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
        }
    }

    private void clientSetup(FMLClientSetupEvent event) {
        LOGGER.info("[ChatSpliter] Initializing...");
        ChatSpliterConfig.getInstance();
        ChatHudManager.getInstance().initialize();
        LOGGER.info("[ChatSpliter] Initialized successfully! Press K to open config.");
    }

    /** Inject a client-side test message with click and hover events. */
    public static void injectDebugMessage(Minecraft client) {
        Component msg = Component.empty()
                .append(Component.literal("[ChatSpliter Debug] ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal("[GitHub]").setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com"))
                        .withColor(ChatFormatting.AQUA)))
                .append(Component.literal(" "))
                .append(Component.literal("[Say Hi]").setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "hi"))
                        .withColor(ChatFormatting.GREEN)))
                .append(Component.literal(" "))
                .append(Component.literal("[Hover]").setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.literal("悬停测试成功！").withStyle(ChatFormatting.YELLOW)))
                        .withColor(ChatFormatting.YELLOW)));

        ChatHudManager.getInstance().onChatMessage(msg, (int) (System.currentTimeMillis() / 50));

        if (client.player != null) {
            client.player.sendSystemMessage(msg);
        }

        LOGGER.info("[ChatSpliter] Debug test message injected.");
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_CONFIG_KEY);
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;
            while (OPEN_CONFIG_KEY.consumeClick()) {
                client.setScreen(new ConfigScreen(null));
            }
        }
    }
}
