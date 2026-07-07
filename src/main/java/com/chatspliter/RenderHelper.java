package com.chatspliter;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

/**
 * Cross-version text rendering via DrawContext.
 * 1.21.8: native call succeeds.
 * 1.21:   NoSuchMethodError caught, silently skipped.
 */
public class RenderHelper {

    public static void drawTextWithShadow(DrawContext ctx, TextRenderer tr, String text, float x, float y, int color) {
        try { ctx.drawTextWithShadow(tr, text, (int)x, (int)y, color); } catch (Throwable ignored) {}
    }

    public static void drawTextWithShadow(DrawContext ctx, TextRenderer tr, Text text, float x, float y, int color) {
        try { ctx.drawTextWithShadow(tr, text, (int)x, (int)y, color); } catch (Throwable ignored) {}
    }

    public static void drawTextWithShadow(DrawContext ctx, TextRenderer tr, OrderedText text, float x, float y, int color) {
        try { ctx.drawTextWithShadow(tr, text, (int)x, (int)y, color); } catch (Throwable ignored) {}
    }

    public static void drawCenteredTextWithShadow(DrawContext ctx, TextRenderer tr, Text text, int centerX, int y, int color) {
        int width = tr.getWidth(text);
        drawTextWithShadow(ctx, tr, text, centerX - width / 2f, y, color);
    }

    public static void drawCenteredTextWithShadow(DrawContext ctx, TextRenderer tr, String text, int centerX, int y, int color) {
        int width = tr.getWidth(text);
        drawTextWithShadow(ctx, tr, text, centerX - width / 2f, y, color);
    }
}
