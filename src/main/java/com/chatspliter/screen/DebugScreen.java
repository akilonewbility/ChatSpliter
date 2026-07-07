package com.chatspliter.screen;

import com.chatspliter.ChatSpliterMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DebugScreen extends Screen {
    private final Screen parent;

    public DebugScreen(Screen parent) {
        super(Text.literal("ChatSpliter Debug"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("发送测试消息"),
                        btn -> ChatSpliterMod.injectDebugMessage(MinecraftClient.getInstance()))
                .dimensions(cx - 60, this.height / 2 - 10, 120, 20).build());

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("完成"),
                        btn -> close())
                .dimensions(cx - 30, this.height - 30, 60, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("点击下方按钮发送带点击/悬停事件的测试消息"), this.width / 2, 40, 0xAAAAAA);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("打开聊天栏(T)后在分离窗口中测试悬停和点击"), this.width / 2, 56, 0x888888);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
