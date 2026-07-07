package com.chatspliter.screen;

import com.chatspliter.RenderHelper;
import com.chatspliter.config.FilterGroup;
import com.chatspliter.config.MatchMode;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupConfigScreen extends Screen {
    private static final int HEADER_H = 26, FOOTER_H = 38, ROW_H = 22;
    private static final int LEFT = 10, RIGHT = 160;
    private static final int SLIDER_X = 252, SLIDER_W = 80, VAL_X = 338;

    private final Screen parent;
    private final FilterGroup group;
    private int x, y, w, h, ls, mh;
    private double sc, dt, ft, op, to;
    private int scrollY, maxScrollY;
    private final Map<Integer, String> tooltips = new LinkedHashMap<>();
    private final List<SliderData> sliders = new ArrayList<>();
    private SliderData dragSlider;

    public GroupConfigScreen(Screen parent, FilterGroup g) {
        super(Text.literal(g.name));
        this.parent = parent;
        this.group = g;
        x = g.x; y = g.y; w = g.width; h = g.height;
        sc = g.scale; dt = g.displayTime; ft = g.fadeTime;
        op = g.opacity; to = g.textOpacity; ls = g.lineSpacing; mh = g.maxHistory;
    }

    @Override
    protected void init() {
        super.init(); tooltips.clear(); sliders.clear();
        int viewH = this.height - HEADER_H - FOOTER_H;
        int totalRows = 21, contentH = totalRows * ROW_H;
        maxScrollY = Math.max(0, contentH - viewH);
        scrollY = MathHelper.clamp(scrollY, 0, maxScrollY);
        int ry = HEADER_H - scrollY;

        addLabel(ry, "chatspliter.filter_group.name", "chatspliter.tooltip.name");
        TextFieldWidget nf = new TextFieldWidget(textRenderer, RIGHT, ry, 160, 20, Text.empty());
        nf.setMaxLength(64); nf.setText(group.name); nf.setChangedListener(v -> group.name = v);
        addDrawableChild(nf); ry += ROW_H;

        addLabel(ry, "chatspliter.filter_group.keywords", "chatspliter.tooltip.keywords");
        int kwW = Math.max(260, this.width - RIGHT - 10);
        TextFieldWidget kf = new TextFieldWidget(textRenderer, RIGHT, ry, kwW, 20, Text.empty());
        kf.setMaxLength(1024); kf.setText(String.join(", ", group.keywords));
        kf.setChangedListener(v -> { group.keywords.clear(); for (String s : v.split(",")) { String t = s.trim(); if (!t.isEmpty()) group.keywords.add(t); }});
        addDrawableChild(kf); ry += ROW_H;

        addLabel(ry, "chatspliter.filter_group.match_mode", "chatspliter.tooltip.match_mode");
        addDrawableChild(CyclingButtonWidget.<MatchMode>builder(m -> Text.translatable(m.getTranslationKey()))
                .values(MatchMode.values()).initially(group.matchMode)
                .build(RIGHT, ry, 120, 20, Text.empty(), (btn, v) -> group.matchMode = v));
        ry += ROW_H;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(group.caseSensitive)
                .build(RIGHT, ry, 120, 20, Text.translatable("chatspliter.filter_group.case_sensitive"), (btn, v) -> group.caseSensitive = v));
        ry += ROW_H;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(group.enabled)
                .build(RIGHT, ry, 120, 20, Text.translatable("chatspliter.config.enabled"), (btn, v) -> group.enabled = v));
        ry += ROW_H;

        ry = addInt("chatspliter.filter_group.x", x, 0, 3840, 10, ry, v -> { x=v; group.x=v; }, "chatspliter.tooltip.x");
        ry = addInt("chatspliter.filter_group.y", y, 0, 2160, 10, ry, v -> { this.y=v; group.y=v; }, "chatspliter.tooltip.y");
        ry = addInt("chatspliter.filter_group.width", w, 60, 1920, 10, ry, v -> { this.w=v; group.width=v; }, "chatspliter.tooltip.width");
        ry = addInt("chatspliter.filter_group.height", h, 40, 1080, 10, ry, v -> { this.h=v; group.height=v; }, "chatspliter.tooltip.height");
        ry = addDbl("chatspliter.filter_group.scale", sc, 0.25, 4.0, 0.05, ry, v -> { sc=v; group.scale=v; }, "chatspliter.tooltip.scale");
        ry = addDbl("chatspliter.filter_group.display_time", dt, 0.5, 60.0, 0.5, ry, v -> { dt=v; group.displayTime=v; }, "chatspliter.tooltip.display_time");
        ry = addDbl("chatspliter.filter_group.fade_time", ft, 0.0, 30.0, 0.5, ry, v -> { ft=v; group.fadeTime=v; }, "chatspliter.tooltip.fade_time");
        ry = addDbl("chatspliter.filter_group.opacity", op, 0.0, 1.0, 0.05, ry, v -> { op=v; group.opacity=v; }, "chatspliter.tooltip.opacity");
        ry = addDbl("chatspliter.filter_group.text_opacity", to, 0.0, 1.0, 0.05, ry, v -> { to=v; group.textOpacity=v; }, "chatspliter.tooltip.text_opacity");
        ry = addInt("chatspliter.filter_group.line_spacing", ls, -5, 10, 1, ry, v -> { ls=v; group.lineSpacing=v; }, "chatspliter.tooltip.line_spacing");
        ry = addInt("历史记录上限", mh, 10, 2000, 10, ry, v -> { mh=v; group.maxHistory=v; }, null);

        addDrawableChild(CyclingButtonWidget.onOffBuilder(group.showTimestamp)
                .build(RIGHT, ry, 120, 20, Text.translatable("chatspliter.filter_group.show_timestamp"), (btn, v) -> group.showTimestamp = v));
        ry += ROW_H;
        addDrawableChild(CyclingButtonWidget.onOffBuilder(group.showTitle)
                .build(RIGHT, ry, 120, 20, Text.translatable("chatspliter.filter_group.show_title"), (btn, v) -> group.showTitle = v));
        ry += ROW_H;
        addDrawableChild(CyclingButtonWidget.<FilterGroup.TextAlign>builder(a -> Text.translatable("chatspliter.filter_group.text_align." + a.name().toLowerCase()))
                .values(FilterGroup.TextAlign.values()).initially(group.textAlign)
                .build(RIGHT, ry, 120, 20, Text.empty(), (btn, v) -> group.textAlign = v));
        ry += ROW_H;
        addDrawableChild(CyclingButtonWidget.<FilterGroup.ScrollDir>builder(a -> Text.translatable("chatspliter.filter_group.scroll_dir." + a.name().toLowerCase()))
                .values(FilterGroup.ScrollDir.values()).initially(group.scrollDir)
                .build(RIGHT, ry, 120, 20, Text.empty(), (btn, v) -> group.scrollDir = v));

        int btnY = this.height - 26;
        addDrawableChild(ButtonWidget.builder(Text.translatable("chatspliter.button.done"), btn -> close())
                .dimensions(this.width - 160, btnY, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("chatspliter.button.cancel"), btn -> close())
                .dimensions(this.width - 80, btnY, 70, 20).build());
    }

    private void addLabel(int ry, String key, String tip) {
        addDrawableChild(new TextWidget(LEFT, ry + 6, 140, 12, Text.translatable(key), textRenderer));
        if (tip != null) tooltips.put(ry, tip);
    }

    private int addInt(String key, int val, int min, int max, int step, int ry, java.util.function.IntConsumer set, String tip) {
        addLabel(ry, key, tip);
        final int cv = val;
        addBtn(Text.literal("-"), RIGHT, ry, 18, () -> { set.accept(MathHelper.clamp(cv - step, min, max)); rebuild(); });
        addBtn(Text.literal("+"), RIGHT + 20, ry, 18, () -> { set.accept(MathHelper.clamp(cv + step, min, max)); rebuild(); });
        sliders.add(new SliderData(ry, true, val, min, max, step, v -> { if (v instanceof Integer iv) set.accept(iv); rebuild(); }));
        addDrawableChild(new TextWidget(VAL_X, ry + 6, 50, 12, Text.literal(String.valueOf(val)), textRenderer));
        return ry + ROW_H;
    }

    private int addDbl(String key, double val, double min, double max, double step, int ry, java.util.function.DoubleConsumer set, String tip) {
        addLabel(ry, key, tip);
        final double cv = val;
        addBtn(Text.literal("-"), RIGHT, ry, 18, () -> { set.accept(Math.round(MathHelper.clamp(cv - step, min, max) * 100.0) / 100.0); rebuild(); });
        addBtn(Text.literal("+"), RIGHT + 20, ry, 18, () -> { set.accept(Math.round(MathHelper.clamp(cv + step, min, max) * 100.0) / 100.0); rebuild(); });
        sliders.add(new SliderData(ry, false, val, min, max, step, v -> { if (v instanceof Double dv) set.accept(dv); rebuild(); }));
        addDrawableChild(new TextWidget(VAL_X, ry + 6, 50, 12, Text.literal(String.format("%.2f", val)), textRenderer));
        return ry + ROW_H;
    }

    private void addBtn(Text text, int bx, int by, int bw, Runnable action) {
        addDrawableChild(ButtonWidget.builder(text, btn -> action.run()).dimensions(bx, by, bw, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        RenderHelper.drawCenteredTextWithShadow(ctx, textRenderer,
                Text.literal(group.name).setStyle(Style.EMPTY.withBold(true)),
                this.width / 2, 6, 0xFFFFFF);
        if (maxScrollY > 0) {
            int vh = this.height - HEADER_H - FOOTER_H;
            int bh = Math.max(16, vh * vh / (21 * ROW_H));
            int by = HEADER_H + (vh - bh) * scrollY / maxScrollY;
            ctx.fill(this.width - 3, by, this.width - 1, by + bh, 0x88AAAAAA);
        }
        // Sliders
        for (SliderData s : sliders) {
            if (s.rowY < HEADER_H || s.rowY > this.height - FOOTER_H) continue;
            double frac = s.isInt
                    ? MathHelper.clamp(((Integer) s.value - s.imin) / (double) (s.imax - s.imin), 0, 1)
                    : MathHelper.clamp(((Double) s.value - s.dmin) / (s.dmax - s.dmin), 0, 1);
            int fill = (int) (frac * SLIDER_W);
            ctx.fill(SLIDER_X, s.rowY + 10, SLIDER_X + SLIDER_W, s.rowY + 12, 0xFF555555);
            ctx.fill(SLIDER_X, s.rowY + 10, SLIDER_X + fill, s.rowY + 12, 0xFF00AA00);
            ctx.fill(SLIDER_X + fill - 2, s.rowY + 7, SLIDER_X + fill + 2, s.rowY + 15, 0xFFFFFFFF);
        }
        // Tooltips
        for (var e : tooltips.entrySet()) {
            if (e.getValue() != null && mx >= LEFT && mx <= this.width - 10 && my >= e.getKey() && my < e.getKey() + ROW_H) {
                try { ctx.drawTooltip(textRenderer, Text.translatable(e.getValue()), mx, my); } catch (Throwable ignored) {}
                break;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (SliderData s : sliders) {
                if (my >= s.rowY && my <= s.rowY + ROW_H && mx >= SLIDER_X && mx <= SLIDER_X + SLIDER_W) {
                    dragSlider = s;
                    applySlider(s, mx);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragSlider != null) { applySlider(dragSlider, mx); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragSlider = null;
        return super.mouseReleased(mx, my, button);
    }

    private void applySlider(SliderData s, double mx) {
        double frac = MathHelper.clamp((mx - SLIDER_X) / SLIDER_W, 0, 1);
        if (s.isInt) {
            int v = Math.round((float) (s.imin + frac * (s.imax - s.imin)));
            v = Math.round((float) v / s.istep) * s.istep;
            v = MathHelper.clamp(v, s.imin, s.imax);
            s.setter.accept(v);
        } else {
            double v = s.dmin + frac * (s.dmax - s.dmin);
            v = Math.round(v / s.dstep) * s.dstep;
            v = MathHelper.clamp(v, s.dmin, s.dmax);
            v = Math.round(v * 100.0) / 100.0;
            s.setter.accept(v);
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hz, double vt) {
        if (maxScrollY > 0) { scrollY = MathHelper.clamp(scrollY - (int)(vt * 12), 0, maxScrollY); rebuild(); return true; }
        return super.mouseScrolled(mx, my, hz, vt);
    }

    private void rebuild() { clearChildren(); init(); }

    @Override public void close() { client.setScreen(parent); }

    private static class SliderData {
        final int rowY; final boolean isInt;
        Object value; final int imin, imax, istep;
        final double dmin, dmax, dstep;
        final java.util.function.Consumer<Object> setter;

        SliderData(int rowY, boolean isInt, Object value, double min, double max, double step, java.util.function.Consumer<Object> setter) {
            this.rowY = rowY; this.isInt = isInt; this.value = value; this.setter = setter;
            this.imin = (int) min; this.imax = (int) max; this.istep = (int) step;
            this.dmin = min; this.dmax = max; this.dstep = step;
        }
    }
}

