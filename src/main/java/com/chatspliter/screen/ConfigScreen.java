package com.chatspliter.screen;

import com.chatspliter.config.ChatSpliterConfig;
import com.chatspliter.config.FilterGroup;
import com.chatspliter.hud.ChatHudManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ChatSpliterConfig config;
    private final List<FilterGroup> workingGroups;

    private CycleButton<Boolean> enabledButton;
    private CycleButton<Boolean> hideMatchedButton;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("chatspliter.config.title"));
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

        enabledButton = CycleButton.onOffBuilder(config.enabled)
                .create(10, 32, 150, 20,
                        Component.translatable("chatspliter.config.enabled"),
                        (btn, val) -> {});
        addRenderableWidget(enabledButton);

        addRenderableWidget(Button.builder(
                        Component.literal("全局设置"),
                        btn -> minecraft.setScreen(new GlobalConfigScreen(this)))
                .bounds(170, 32, 60, 20).build());

        hideMatchedButton = CycleButton.onOffBuilder(config.hideMatchedFromMain)
                .create(10, 56, 220, 20,
                        Component.translatable("chatspliter.config.hide_matched_from_main"),
                        (btn, val) -> {});
        addRenderableWidget(hideMatchedButton);

        int y = 84;
        int maxVisible = Math.min(workingGroups.size(), (this.height - 170) / 30);

        for (int i = 0; i < maxVisible; i++) {
            final int index = i;
            FilterGroup group = workingGroups.get(i);

            String statusIcon = group.enabled ? "§a●" : "§7○";

            addRenderableWidget(Button.builder(
                            Component.literal("⚙"),
                            btn -> openGroupEditor(index))
                    .bounds(this.width - 90, y, 20, 20).build());

            addRenderableWidget(Button.builder(
                            Component.literal("✕"),
                            btn -> removeGroup(index))
                    .bounds(this.width - 65, y, 20, 20).build());

            y += 28;
        }

        int buttonY = this.height - 28;

        addRenderableWidget(Button.builder(
                        Component.translatable("chatspliter.button.add_group"),
                        btn -> addGroup())
                .bounds(10, buttonY, 120, 20).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("chatspliter.button.reset"),
                        btn -> resetDefaults())
                .bounds(135, buttonY, 75, 20).build());

        addRenderableWidget(Button.builder(
                        Component.literal("调试"),
                        btn -> minecraft.setScreen(new DebugScreen(this)))
                .bounds(215, buttonY, 50, 20).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("chatspliter.button.done"),
                        btn -> saveAndClose())
                .bounds(this.width - 160, buttonY, 70, 20).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("chatspliter.button.cancel"),
                        btn -> onClose())
                .bounds(this.width - 80, buttonY, 70, 20).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Title
        context.drawCenteredString(font,
                Component.translatable("chatspliter.config.title"),
                this.width / 2, 8, 0xFFFFFF);

        // Filter groups label
        context.drawString(font,
                Component.translatable("chatspliter.config.filter_groups"),
                10, 84, 0xAAAAAA);

        // Group entries
        int y = 90;
        int maxVisible = Math.min(workingGroups.size(), (this.height - 170) / 30);
        for (int i = 0; i < maxVisible; i++) {
            FilterGroup group = workingGroups.get(i);
            String statusIcon = group.enabled ? "§a●" : "§7○";
            context.drawString(font,
                    Component.literal(statusIcon + " " + group.name + "  §7[" + group.keywords.size() + " kw]"),
                    14, y + 6, 0xFFFFFF);
            y += 28;
        }

        // Counter
        context.drawString(font,
                Component.literal(workingGroups.size() + " groups"),
                this.width - 100, 86, 0x666666);
    }

    private void openGroupEditor(int index) {
        if (index >= 0 && index < workingGroups.size()) {
            minecraft.setScreen(new GroupConfigScreen(this, workingGroups.get(index)));
        }
    }

    private void addGroup() {
        FilterGroup newGroup = new FilterGroup("Group " + (workingGroups.size() + 1));
        workingGroups.add(newGroup);
        clearWidgets();
        init();
    }

    private void removeGroup(int index) {
        if (index >= 0 && index < workingGroups.size()) {
            workingGroups.remove(index);
            clearWidgets();
            init();
        }
    }

    private void saveAndClose() {
        config.enabled = enabledButton.getValue();
        config.hideMatchedFromMain = hideMatchedButton.getValue();

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
        onClose();
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
        dst.maxHistory = src.maxHistory;
        dst.showTitle = src.showTitle;
        dst.textAlign = src.textAlign;
        dst.scrollDir = src.scrollDir;
        dst.caseSensitive = src.caseSensitive;
    }

    private void resetDefaults() {
        config.reset();
        workingGroups.clear();
        for (FilterGroup g : config.filterGroups) {
            workingGroups.add(g.copy());
        }
        clearWidgets();
        init();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
