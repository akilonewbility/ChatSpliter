package com.chatspliter.hud;

import com.chatspliter.RenderHelper;
import com.chatspliter.config.ChatSpliterConfig;
import com.chatspliter.config.FilterGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.lang.reflect.Method;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FilteredChatHud {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public interface SettingsOpener {
        void openSettings(FilterGroup group);
    }

    private FilterGroup config;
    private final List<FilteredMessage> messages = new ArrayList<>();
    private final MinecraftClient client;
    private int scrolledLines;

    // Settings button bounds (set during render)
    private int gearX, gearY, gearW, gearH;

    // Rendered line bounds for hover/click detection
    private final List<LineBounds> lineBounds = new ArrayList<>();

    // Drag
    private boolean dragging, resizing;
    private int dsx, dsy, dox, doy, dow, doh;

    public FilteredChatHud(FilterGroup config) {
        this.config = config;
        this.client = MinecraftClient.getInstance();
    }

    public FilterGroup getConfig() { return config; }

    /** Update config reference without clearing message history */
    public void replaceConfig(FilterGroup newConfig) {
        this.config = newConfig;
    }

    public void addMessage(Text message, int tickCounter) {
        if (!config.enabled || !config.matches(message.getString())) return;
        messages.add(new FilteredMessage(message.copy(), System.currentTimeMillis(),
                LocalTime.now().format(TIME_FMT)));
        while (messages.size() > config.maxHistory) messages.remove(0);
        scrolledLines = 0;
    }

    public void clear() { messages.clear(); scrolledLines = 0; }

    public void refresh() {
        int mins = ChatSpliterConfig.getInstance().historyMinutes;
        long cutoff = mins <= 0 ? 0 : System.currentTimeMillis() - (long) mins * 60_000L;
        if (cutoff > 0) messages.removeIf(m -> m.receivedTime < cutoff);
        while (messages.size() > config.maxHistory) messages.remove(0);
    }

    // ==================== Render ====================

    public void render(DrawContext ctx, int tickCounter, int sw, int sh, SettingsOpener opener) {
        if (!config.enabled) return;

        int x = MathHelper.clamp(config.x, 0, sw - config.width);
        int y = MathHelper.clamp(config.y, 0, sh - config.height);
        int w = config.width;
        int h = config.height;
        boolean chatOpen = isChatOpen();

        // Apply text scale via matrix transform (cross-version via reflection)
        float s = (float) config.scale;
        boolean useScale = s != 1.0f && s > 0.1f;
        Object matrixStack = null;
        if (useScale) {
            matrixStack = getMatrixStack(ctx);
            if (matrixStack != null) {
                pushMatrix(matrixStack);
                translateMatrix(matrixStack, (float) x, (float) y, 0);
                scaleMatrix(matrixStack, s, s, 1);
                translateMatrix(matrixStack, (float) -x, (float) -y, 0);
            }
        }

        if (!messages.isEmpty()) renderMessages(ctx, x, y, w, h, useScale && matrixStack != null);

        // Hover tooltip rendering (outside scale matrix)
        if (!lineBounds.isEmpty()) {
            double mx = client.mouse.getX() / client.getWindow().getScaleFactor();
            double my = client.mouse.getY() / client.getWindow().getScaleFactor();
            Style hovered = getHoveredStyle(mx, my, sw, sh);
            if (hovered != null) renderHoverTooltip(ctx, hovered.getHoverEvent(), (int) mx, (int) my);
        }

        if (useScale && matrixStack != null) {
            popMatrix(matrixStack);
        }

        // Edit border + settings button (not scaled)
        if (chatOpen) {
            int b = 0x88FFCC00;
            ctx.fill(x, y, x + w, y + 1, b);
            ctx.fill(x, y + h - 1, x + w, y + h, b);
            ctx.fill(x, y, x + 1, y + h, b);
            ctx.fill(x + w - 1, y, x + w, y + h, b);
            ctx.fill(x + w - 8, y + h - 8, x + w, y + h, 0x66FFCC00);

            // Settings gear button (top-right corner)
            String gear = "⚙";
            int gw = client.textRenderer.getWidth(gear) + 4;
            gearX = x + w - gw - 2;
            gearY = y + 3;
            gearW = gw;
            gearH = 11;
            ctx.fill(gearX, gearY, gearX + gearW, gearY + gearH, 0x66000000);
            RenderHelper.drawTextWithShadow(ctx, client.textRenderer, Text.literal(gear),
                    gearX + 2, gearY + 1, 0xFFFFCC00);

            if (messages.isEmpty()) {
                String lbl = config.name;
                int lw = client.textRenderer.getWidth(lbl);
                RenderHelper.drawTextWithShadow(ctx, client.textRenderer, Text.literal(lbl),
                        x + (w - lw) / 2, y + h / 2 - 4, 0x88AAAAAA);
            }
        }

        // Title — always show when enabled and messages exist
        if (config.showTitle && !messages.isEmpty()) {
            String lbl = config.name;
            RenderHelper.drawTextWithShadow(ctx, client.textRenderer, Text.literal(lbl),
                    x + w - client.textRenderer.getWidth(lbl) - 4, y + 1, 0x55AAAAAA);
        }
    }

    private void renderMessages(DrawContext ctx, int bx, int by, int w, int h, boolean scaled) {
        TextRenderer tr = client.textRenderer;
        float s = (float) config.scale;
        int effW = scaled ? (int) (w / s) : w;
        int effH = scaled ? (int) (h / s) : h;
        int lh = 9 + config.lineSpacing;
        int pad = 2;
        int textW = effW - 4;
        if (textW <= 0 || lh <= 0) return;

        boolean chatOpen = isChatOpen();
        long now = System.currentTimeMillis();

        List<RenderLine> allLines = new ArrayList<>();
        for (FilteredMessage msg : messages) {
            Text dt = msg.content;
            if (config.showTimestamp) dt = Text.literal("[" + msg.timestamp + "] ").append(dt);
            for (OrderedText ot : tr.wrapLines(dt, textW))
                allLines.add(new RenderLine(ot, msg.receivedTime));
        }
        if (allLines.isEmpty()) return;

        int total = allLines.size();
        int maxVis = effH / lh;
        if (maxVis <= 0) return;

        int start;
        if (chatOpen) {
            start = Math.max(0, total - maxVis - scrolledLines);
        } else {
            start = Math.max(0, total - maxVis);
            scrolledLines = 0;
        }
        int end = Math.min(total, start + maxVis);

        FilterGroup.ScrollDir dir = config.scrollDir;
        boolean rightAlign = config.textAlign == FilterGroup.TextAlign.RIGHT;
        boolean anchored = dir == FilterGroup.ScrollDir.TOP_ANCHORED;

        // Iteration: newest→oldest for BOTTOM_UP and TOP_DOWN; oldest→newest for TOP_ANCHORED
        int i = anchored ? start : (end - 1);
        int di = anchored ? 1 : -1;
        int n = 0;

        lineBounds.clear();

        while (anchored ? (i < end) : (i >= start)) {
            RenderLine rl = allLines.get(i);
            long ageMs = now - rl.receivedTime;
            int alpha = chatOpen ? 255 : calcFade(ageMs);

            // TOP_ANCHORED: skip fully faded lines, they don't take space
            if (anchored && alpha <= 2) { i += di; continue; }

            int lineY;
            if (dir == FilterGroup.ScrollDir.BOTTOM_UP) {
                lineY = by + effH - pad - lh - n * lh;
                if (lineY < by) break;
            } else {
                lineY = by + pad + n * lh;
                if (lineY + lh > by + effH) break;
            }

            if (alpha > 2) {
                int tw = tr.getWidth(rl.text);
                int textX = rightAlign ? (bx + effW - 2 - tw) : (bx + 2);
                int bgL = rightAlign ? Math.max(bx + 1, bx + effW - 2 - tw - 2) : (bx + 1);
                int bgR = rightAlign ? (bx + effW - 1) : Math.min(bx + effW - 1, bx + 1 + tw + 4);

                int bgA = (int) (config.opacity * alpha * 0.7) & 0xFF;
                if (bgA > 10)
                    ctx.fill(bgL, lineY - 1, bgR, lineY + lh - 1, (bgA << 24) | (config.backgroundColor & 0x00FFFFFF));

                int tc = config.textColor & 0x00FFFFFF;
                int ma = (int) (config.textOpacity * alpha) & 0xFF;
                RenderHelper.drawTextWithShadow(ctx, tr, rl.text, textX, lineY, (ma << 24) | tc);

                lineBounds.add(new LineBounds(rl.text, textX, lineY, tw, scaled));
            }
            n++;
            if (n >= maxVis) break;
            i += di;
        }
    }

    private int calcFade(long ageMs) {
        double ageSec = ageMs / 1000.0;
        double total = config.displayTime;
        double fade = config.fadeTime;
        if (fade <= 0 || total <= 0) return ageSec < total ? 255 : 0;
        double fullSec = Math.max(0, total - fade);
        if (ageSec < fullSec) return 255;
        if (ageSec >= total) return 0;
        return (int) (255 * (1.0 - (ageSec - fullSec) / fade));
    }

    private void renderHoverTooltip(DrawContext ctx, HoverEvent event, int mx, int my) {
        if (event instanceof HoverEvent.ShowText st) {
            Text value = st.value();
            String raw = value.getString();
            if (raw.contains("\n")) {
                Style style = value.getStyle();
                List<Text> lines = new ArrayList<>();
                for (String line : raw.split("\n"))
                    lines.add(Text.literal(line).setStyle(style));
                try { ctx.drawTooltip(client.textRenderer, lines, mx, my); } catch (Throwable ignored) {}
            } else {
                try { ctx.drawTooltip(client.textRenderer, value, mx, my); } catch (Throwable ignored) {}
            }
        }
    }

    // ==================== Hover / Click ====================

    public Style getHoveredStyle(double mx, double my, int sw, int sh) {
        int hx = MathHelper.clamp(config.x, 0, sw - config.width);
        int hy = MathHelper.clamp(config.y, 0, sh - config.height);
        float s = (float) config.scale;

        for (LineBounds lb : lineBounds) {
            int lx, ly, lw;
            if (lb.scaled) {
                lx = hx + (int)((lb.x - hx) * s);
                ly = hy + (int)((lb.y - hy) * s);
                lw = (int)(lb.w * s);
            } else {
                lx = lb.x;
                ly = lb.y;
                lw = lb.w;
            }
            if (mx >= lx && mx <= lx + lw && my >= ly && my <= ly + 9) {
                int offset = (int) (mx - lx);
                offset = lb.scaled ? (int)(offset / s) : offset;
                return client.textRenderer.getTextHandler().getStyleAt(lb.text, offset);
            }
        }
        return null;
    }

    public boolean handleClick(double mx, double my, int sw, int sh) {
        Style style = getHoveredStyle(mx, my, sw, sh);
        if (style == null || style.getClickEvent() == null) return false;
        if (client.currentScreen != null)
            return client.currentScreen.handleTextClick(style);
        return false;
    }

    // ==================== Mouse ====================

    public boolean isMouseOver(double mx, double my, int sw, int sh) {
        int x = MathHelper.clamp(config.x, 0, sw - config.width);
        int y = MathHelper.clamp(config.y, 0, sh - config.height);
        return mx >= x && mx <= x + config.width && my >= y && my <= y + config.height;
    }

    public boolean isOverSettingsButton(double mx, double my) {
        return mx >= gearX && mx <= gearX + gearW && my >= gearY && my <= gearY + gearH;
    }

    public int getInteractionZone(double mx, double my, int sw, int sh) {
        int x = MathHelper.clamp(config.x, 0, sw - config.width);
        int y = MathHelper.clamp(config.y, 0, sh - config.height);
        if (mx < x || mx > x + config.width || my < y || my > y + config.height) return 0;
        if (isOverSettingsButton(mx, my)) return 3; // settings button
        if (mx > x + config.width - 12 && my > y + config.height - 12) return 2; // resize
        return 1; // move
    }

    public void startDrag(int mx, int my, int zone, int sw, int sh) {
        dragging = zone == 1; resizing = zone == 2;
        dsx = mx; dsy = my; dox = config.x; doy = config.y;
        dow = config.width; doh = config.height;
    }

    public void onDrag(int mx, int my, int sw, int sh) {
        if (dragging) {
            config.x = MathHelper.clamp(dox + (mx - dsx), 0, sw - config.width);
            config.y = MathHelper.clamp(doy + (my - dsy), 0, sh - config.height);
        } else if (resizing) {
            config.width = MathHelper.clamp(dow + (mx - dsx), 60, sw - config.x);
            config.height = MathHelper.clamp(doh + (my - dsy), 40, sh - config.y);
        }
    }

    public void endDrag() {
        if (dragging || resizing) { ChatSpliterConfig.getInstance().save(); dragging = false; resizing = false; }
    }

    public boolean isDragging() { return dragging || resizing; }

    public void onScroll(double amount) {
        int maxS = Math.max(0, messages.size() - 1);
        scrolledLines = MathHelper.clamp(scrolledLines + (int) amount, 0, maxS);
    }

    private boolean isChatOpen() { return client.currentScreen instanceof ChatScreen; }

    private record FilteredMessage(Text content, long receivedTime, String timestamp) {}
    private record RenderLine(OrderedText text, long receivedTime) {}
    private record LineBounds(OrderedText text, int x, int y, int w, boolean scaled) {}

    // ==================== Cross-version matrix reflection ====================

    private static Object getMatrixStack(DrawContext ctx) {
        try {
            return GET_MATRICES.invoke(ctx);
        } catch (Exception e) {
            return null;
        }
    }

    private static void pushMatrix(Object stack) {
        try { PUSH.invoke(stack); } catch (Exception ignored) {}
    }

    private static void popMatrix(Object stack) {
        try { POP.invoke(stack); } catch (Exception ignored) {}
    }

    private static void translateMatrix(Object stack, float x, float y, float z) {
        try { TRANSLATE.invoke(stack, x, y, z); } catch (Exception ignored) {}
    }

    private static void scaleMatrix(Object stack, float x, float y, float z) {
        try { SCALE.invoke(stack, x, y, z); } catch (Exception ignored) {}
    }

    private static final Method GET_MATRICES;
    private static final Method PUSH;
    private static final Method POP;
    private static final Method TRANSLATE;
    private static final Method SCALE;

    static {
        Method gm = null, push = null, pop = null, tr = null, sc = null;
        try {
            gm = DrawContext.class.getMethod("getMatrices");
            Class<?> ms = gm.getReturnType();
            for (Method m : ms.getMethods()) {
                if (m.getName().equals("push") && m.getParameterCount() == 0) push = m;
                else if (m.getName().equals("pop") && m.getParameterCount() == 0) pop = m;
                else if (m.getName().equals("translate") && m.getParameterCount() == 3) tr = m;
                else if (m.getName().equals("scale") && m.getParameterCount() == 3) sc = m;
            }
        } catch (Exception ignored) {}
        GET_MATRICES = gm;
        PUSH = push;
        POP = pop;
        TRANSLATE = tr;
        SCALE = sc;
    }
}

