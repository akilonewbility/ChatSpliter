package com.chatspliter.screen;

import com.chatspliter.config.ChatSpliterConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class GlobalConfigScreen extends Screen {
    private static final int HISTORY_MAX = 1440;
    private final Screen parent;
    private final ChatSpliterConfig config;
    private int sliderX, sliderY, sliderW;
    private boolean dragging;

    public GlobalConfigScreen(Screen parent) {
        super(Text.literal("全局设置"));
        this.parent = parent;
        this.config = ChatSpliterConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        sliderW = Math.min(300, this.width - 80);
        sliderX = (this.width - sliderW) / 2;
        sliderY = 60;

        int btnY = this.height - 30;
        addDrawableChild(ButtonWidget.builder(Text.literal("完成"), btn -> { config.save(); close(); })
                .dimensions(this.width - 160, btnY, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("取消"), btn -> close())
                .dimensions(this.width - 80, btnY, 70, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§l全局设置"), this.width / 2, 10, 0xFFFFFF);

        String value = config.historyMinutes <= 0 ? "无限制"
                : (config.historyMinutes < 60 ? config.historyMinutes + " 分钟"
                : (config.historyMinutes / 60) + " 小时"
                  + (config.historyMinutes % 60 > 0 ? " " + config.historyMinutes % 60 + " 分钟" : ""));
        String label = "历史记录保留时间：" + value;
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(label), this.width / 2, 38, 0xFFAAAAAA);

        // Slider track
        double frac = MathHelper.clamp(config.historyMinutes / (double) HISTORY_MAX, 0, 1);
        int fill = (int) (frac * sliderW);
        ctx.fill(sliderX, sliderY + 8, sliderX + sliderW, sliderY + 10, 0xFF555555);
        ctx.fill(sliderX, sliderY + 8, sliderX + fill, sliderY + 10, 0xFF00AA00);
        ctx.fill(sliderX + fill - 3, sliderY + 4, sliderX + fill + 3, sliderY + 14, 0xFFFFFFFF);

        // Range labels
        ctx.drawTextWithShadow(textRenderer, Text.literal("无限制"), sliderX, sliderY + 16, 0x666666);
        String maxLabel = (HISTORY_MAX / 60) + " 小时";
        ctx.drawTextWithShadow(textRenderer, Text.literal(maxLabel),
                sliderX + sliderW - textRenderer.getWidth(maxLabel), sliderY + 16, 0x666666);

        if (dragging) {
            double newFrac = MathHelper.clamp((mx - sliderX) / (double) sliderW, 0, 1);
            if (newFrac < 0.5 / HISTORY_MAX) config.historyMinutes = 0;
            else config.historyMinutes = Math.max(1, (int) Math.round(newFrac * HISTORY_MAX));
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && mx >= sliderX && mx <= sliderX + sliderW && my >= sliderY && my <= sliderY + 14) {
            dragging = true;
            double frac = MathHelper.clamp((mx - sliderX) / (double) sliderW, 0, 1);
            setFromFrac(frac);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            double frac = MathHelper.clamp((mx - sliderX) / (double) sliderW, 0, 1);
            setFromFrac(frac);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    private void setFromFrac(double frac) {
        if (frac < 0.5 / HISTORY_MAX) config.historyMinutes = 0;
        else config.historyMinutes = Math.max(1, (int) Math.round(frac * HISTORY_MAX));
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public void close() { client.setScreen(parent); }
}
