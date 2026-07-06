package com.chatspliter.screen;

import com.chatspliter.config.ChatSpliterConfig;
import com.chatspliter.config.FilterGroup;
import com.chatspliter.hud.ChatHudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ChatSpliterConfig config;
    private final List<FilterGroup> workingGroups;

    private CyclingButtonWidget<Boolean> enabledButton;
    private CyclingButtonWidget<Boolean> hideMatchedButton;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("chatspliter.config.title"));
        this.parent = parent;
        this.config = ChatSpliterConfig.getInstance();
        this.workingGroups = new ArrayList<>();
        for (FilterGroup g : config.filterGroups) {
            this.workingGroups.add(g.copy());
        }
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;

        addDrawableChild(new TextWidget(centerX - 80, 8, 160, 20,
                Text.translatable("chatspliter.config.title"), textRenderer).alignCenter());

        enabledButton = CyclingButtonWidget.onOffBuilder(config.enabled)
                .build(10, 32, 150, 20,
                        Text.translatable("chatspliter.config.enabled"),
                        (btn, val) -> {});
        addDrawableChild(enabledButton);

        hideMatchedButton = CyclingButtonWidget.onOffBuilder(config.hideMatchedFromMain)
                .build(10, 56, 220, 20,
                        Text.translatable("chatspliter.config.hide_matched_from_main"),
                        (btn, val) -> {});
        addDrawableChild(hideMatchedButton);

        addDrawableChild(new TextWidget(10, 84, 200, 12,
                Text.translatable("chatspliter.config.filter_groups"), textRenderer));

        int y = 100;
        int maxVisible = Math.min(workingGroups.size(), (this.height - 170) / 30);

        for (int i = 0; i < maxVisible; i++) {
            final int index = i;
            FilterGroup group = workingGroups.get(i);

            String statusIcon = group.enabled ? "§a●" : "§7○";
            addDrawableChild(new TextWidget(14, y + 6, 200, 12,
                    Text.literal(statusIcon + " " + group.name + "  §7[" + group.keywords.size() + " kw]"),
                    textRenderer));

            addDrawableChild(ButtonWidget.builder(
                            Text.literal("⚙"),
                            btn -> openGroupEditor(index))
                    .dimensions(this.width - 90, y, 20, 20).build());

            addDrawableChild(ButtonWidget.builder(
                            Text.literal("✕"),
                            btn -> removeGroup(index))
                    .dimensions(this.width - 65, y, 20, 20).build());

            y += 28;
        }

        int buttonY = this.height - 28;

        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("chatspliter.button.add_group"),
                        btn -> addGroup())
                .dimensions(10, buttonY, 120, 20).build());

        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("chatspliter.button.reset"),
                        btn -> resetDefaults())
                .dimensions(135, buttonY, 75, 20).build());

        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("chatspliter.button.done"),
                        btn -> saveAndClose())
                .dimensions(this.width - 160, buttonY, 70, 20).build());

        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("chatspliter.button.cancel"),
                        btn -> close())
                .dimensions(this.width - 80, buttonY, 70, 20).build());
    }

    private void openGroupEditor(int index) {
        if (index >= 0 && index < workingGroups.size()) {
            client.setScreen(new GroupConfigScreen(this, workingGroups.get(index)));
        }
    }

    private void addGroup() {
        FilterGroup newGroup = new FilterGroup("Group " + (workingGroups.size() + 1));
        workingGroups.add(newGroup);
        clearChildren();
        init();
    }

    private void removeGroup(int index) {
        if (index >= 0 && index < workingGroups.size()) {
            workingGroups.remove(index);
            clearChildren();
            init();
        }
    }

    private void saveAndClose() {
        config.enabled = enabledButton.getValue();
        config.hideMatchedFromMain = hideMatchedButton.getValue();

        // Update in-place: keep existing FilterGroup objects so HUDs retain history
        while (config.filterGroups.size() > workingGroups.size())
            config.filterGroups.remove(config.filterGroups.size() - 1);
        for (int i = 0; i < workingGroups.size(); i++) {
            FilterGroup src = workingGroups.get(i);
            if (i < config.filterGroups.size()) {
                copyInto(src, config.filterGroups.get(i));
            } else {
                config.filterGroups.add(src.copy());
            }
        }
        config.save();
        ChatHudManager.getInstance().syncFromConfig();
        close();
    }

    private void copyInto(FilterGroup src, FilterGroup dst) {
        dst.name = src.name;
        dst.enabled = src.enabled;
        dst.keywords.clear(); dst.keywords.addAll(src.keywords);
        dst.matchMode = src.matchMode;
        dst.x = src.x; dst.y = src.y;
        dst.width = src.width; dst.height = src.height;
        dst.scale = src.scale;
        dst.displayTime = src.displayTime;
        dst.fadeTime = src.fadeTime;
        dst.opacity = src.opacity;
        dst.textOpacity = src.textOpacity;
        dst.lineSpacing = src.lineSpacing;
        dst.showTimestamp = src.showTimestamp;
        dst.maxLines = src.maxLines;
        dst.showTitle = src.showTitle;
    }

    private void resetDefaults() {
        config.reset();
        workingGroups.clear();
        for (FilterGroup g : config.filterGroups) {
            workingGroups.add(g.copy());
        }
        init();
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(textRenderer,
                Text.literal(workingGroups.size() + " groups"),
                this.width - 100, 86, 0x666666);
    }
}
