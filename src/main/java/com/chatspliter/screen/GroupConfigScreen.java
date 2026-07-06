package com.chatspliter.screen;

import com.chatspliter.config.FilterGroup;
import com.chatspliter.config.MatchMode;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedHashMap;
import java.util.Map;

public class GroupConfigScreen extends Screen {
    private static final int HEADER_H = 26;
    private static final int FOOTER_H = 38;
    private static final int ROW_H = 22;
    private static final int LEFT = 10;
    private static final int RIGHT = 160;

    private final Screen parent;
    private final FilterGroup group;

    // Working copies
    private int x, y, w, h, ls, ml;
    private double sc, dt, ft, op, to;
    private int scrollY, maxScrollY;

    // Tooltip mapping: rowY -> tooltip translation key
    private final Map<Integer, String> tooltips = new LinkedHashMap<>();

    public GroupConfigScreen(Screen parent, FilterGroup g) {
        super(Text.literal(g.name));
        this.parent = parent;
        this.group = g;
        x = g.x; y = g.y; w = g.width; h = g.height;
        sc = g.scale; dt = g.displayTime; ft = g.fadeTime;
        op = g.opacity; to = g.textOpacity; ls = g.lineSpacing; ml = g.maxLines;
    }

    @Override
    protected void init() {
        super.init();
        tooltips.clear();

        int viewH = this.height - HEADER_H - FOOTER_H;
        int totalRows = 22, contentH = totalRows * ROW_H;
        maxScrollY = Math.max(0, contentH - viewH);
        scrollY = MathHelper.clamp(scrollY, 0, maxScrollY);

        int ry = HEADER_H - scrollY;

        // Name
        addLabel(ry, "chatspliter.filter_group.name", "chatspliter.tooltip.name");
        TextFieldWidget nf = new TextFieldWidget(textRenderer, RIGHT, ry, 160, 20, Text.empty());
        nf.setMaxLength(64);
        nf.setText(group.name); nf.setChangedListener(v -> group.name = v);
        addDrawableChild(nf);
        ry += ROW_H;

        // Keywords
        addLabel(ry, "chatspliter.filter_group.keywords", "chatspliter.tooltip.keywords");
        int kwW = Math.max(260, this.width - RIGHT - 10);
        TextFieldWidget kf = new TextFieldWidget(textRenderer, RIGHT, ry, kwW, 20, Text.empty());
        kf.setMaxLength(1024);
        kf.setText(String.join(", ", group.keywords));
        kf.setChangedListener(v -> { group.keywords.clear(); for (String s : v.split(",")) { String t = s.trim(); if (!t.isEmpty()) group.keywords.add(t); }});
        addDrawableChild(kf);
        ry += ROW_H;

        // Match mode
        addLabel(ry, "chatspliter.filter_group.match_mode", "chatspliter.tooltip.match_mode");
        addDrawableChild(CyclingButtonWidget.<MatchMode>builder(m -> Text.translatable(m.getTranslationKey()))
                .values(MatchMode.values()).initially(group.matchMode)
                .build(RIGHT, ry, 120, 20, Text.empty(), (btn, v) -> group.matchMode = v));
        ry += ROW_H;

        // Case sensitive
        addDrawableChild(CyclingButtonWidget.onOffBuilder(group.caseSensitive)
                .build(RIGHT, ry, 120, 20, Text.translatable("chatspliter.filter_group.case_sensitive"),
                        (btn, v) -> group.caseSensitive = v));
        ry += ROW_H;

        // Enabled
        addDrawableChild(CyclingButtonWidget.onOffBuilder(group.enabled)
                .build(RIGHT, ry, 120, 20, Text.translatable("chatspliter.config.enabled"),
                        (btn, v) -> group.enabled = v));
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
        ry = addInt("chatspliter.filter_group.max_lines", ml, 10, 500, 10, ry, v -> { ml=v; group.maxLines=v; }, "chatspliter.tooltip.max_lines");

        addDrawableChild(CyclingButtonWidget.onOffBuilder(group.showTimestamp)
                .build(RIGHT, ry, 120, 20, Text.translatable("chatspliter.filter_group.show_timestamp"),
                        (btn, v) -> group.showTimestamp = v));
        ry += ROW_H;

        // Show title
        addDrawableChild(CyclingButtonWidget.onOffBuilder(group.showTitle)
                .build(RIGHT, ry, 120, 20, Text.translatable("chatspliter.filter_group.show_title"),
                        (btn, v) -> group.showTitle = v));
        ry += ROW_H;

        // Text align
        addDrawableChild(CyclingButtonWidget.<FilterGroup.TextAlign>builder(
                        a -> Text.translatable("chatspliter.filter_group.text_align." + a.name().toLowerCase()))
                .values(FilterGroup.TextAlign.values()).initially(group.textAlign)
                .build(RIGHT, ry, 120, 20, Text.empty(),
                        (btn, v) -> group.textAlign = v));
        ry += ROW_H;

        // Scroll direction
        addDrawableChild(CyclingButtonWidget.<FilterGroup.ScrollDir>builder(
                        a -> Text.translatable("chatspliter.filter_group.scroll_dir." + a.name().toLowerCase()))
                .values(FilterGroup.ScrollDir.values()).initially(group.scrollDir)
                .build(RIGHT, ry, 120, 20, Text.empty(),
                        (btn, v) -> group.scrollDir = v));

        // Footer buttons
        int btnY = this.height - 26;
        addDrawableChild(ButtonWidget.builder(Text.translatable("chatspliter.button.done"), btn -> close())
                .dimensions(this.width - 160, btnY, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("chatspliter.button.cancel"), btn -> close())
                .dimensions(this.width - 80, btnY, 70, 20).build());
    }

    private void addLabel(int ry, String key, String tip) {
        addDrawableChild(new TextWidget(LEFT, ry + 6, 140, 12, Text.translatable(key), textRenderer));
        tooltips.put(ry, tip);
    }

    private int addInt(String key, int val, int min, int max, int step, int ry, java.util.function.IntConsumer set, String tip) {
        addLabel(ry, key, tip);
        final int cv = val;
        addBtn(Text.literal(" - "), RIGHT, ry, 22, () -> { set.accept(MathHelper.clamp(cv - step, min, max)); rebuild(); });
        addDrawableChild(new TextWidget(RIGHT + 30, ry + 6, 60, 12, Text.literal(String.valueOf(val)), textRenderer));
        addBtn(Text.literal(" + "), RIGHT + 80, ry, 22, () -> { set.accept(MathHelper.clamp(cv + step, min, max)); rebuild(); });
        return ry + ROW_H;
    }

    private int addDbl(String key, double val, double min, double max, double step, int ry, java.util.function.DoubleConsumer set, String tip) {
        addLabel(ry, key, tip);
        final double cv = val;
        addBtn(Text.literal(" - "), RIGHT, ry, 22, () -> { set.accept(Math.round(MathHelper.clamp(cv - step, min, max) * 100.0) / 100.0); rebuild(); });
        addDrawableChild(new TextWidget(RIGHT + 30, ry + 6, 60, 12, Text.literal(String.format("%.2f", val)), textRenderer));
        addBtn(Text.literal(" + "), RIGHT + 80, ry, 22, () -> { set.accept(Math.round(MathHelper.clamp(cv + step, min, max) * 100.0) / 100.0); rebuild(); });
        return ry + ROW_H;
    }

    private void addBtn(Text text, int bx, int by, int bw, Runnable action) {
        addDrawableChild(ButtonWidget.builder(text, btn -> action.run()).dimensions(bx, by, bw, 20).build());
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hz, double vt) {
        if (maxScrollY > 0) { scrollY = MathHelper.clamp(scrollY - (int)(vt * 12), 0, maxScrollY); rebuild(); return true; }
        return super.mouseScrolled(mx, my, hz, vt);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§l" + group.name), this.width / 2, 6, 0xFFFFFF);

        // Scrollbar
        if (maxScrollY > 0) {
            int vh = this.height - HEADER_H - FOOTER_H;
            int bh = Math.max(16, vh * vh / (22 * ROW_H));
            int by = HEADER_H + (vh - bh) * scrollY / maxScrollY;
            ctx.fill(this.width - 3, by, this.width - 1, by + bh, 0x88AAAAAA);
        }

        // Tooltip: find which row the mouse is on
        for (var entry : tooltips.entrySet()) {
            int rowTop = entry.getKey();
            if (mx >= LEFT && mx <= this.width - 10 && my >= rowTop && my < rowTop + ROW_H) {
                ctx.drawTooltip(textRenderer, Text.translatable(entry.getValue()), mx, my);
                break;
            }
        }
    }

    private void rebuild() { clearChildren(); init(); }

    @Override public void close() { client.setScreen(parent); }
}
