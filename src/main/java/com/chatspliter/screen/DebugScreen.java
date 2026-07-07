package com.chatspliter.screen;

import com.chatspliter.ChatSpliterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DebugScreen extends Screen {
    private final Screen parent;

    public DebugScreen(Screen parent) {
        super(Component.literal("ChatSpliter Debug"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;

        addRenderableWidget(Button.builder(
                        Component.literal("发送测试消息"),
                        btn -> ChatSpliterMod.injectDebugMessage(Minecraft.getInstance()))
                .bounds(cx - 60, this.height / 2 - 10, 120, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("完成"),
                        btn -> onClose())
                .bounds(cx - 30, this.height - 30, 60, 20).build());
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredString(font,
                Component.literal("点击下方按钮发送带点击/悬停事件的测试消息"), this.width / 2, 40, 0xAAAAAA);
        ctx.drawCenteredString(font,
                Component.literal("打开聊天栏(T)后在分离窗口中测试悬停和点击"), this.width / 2, 56, 0x888888);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
