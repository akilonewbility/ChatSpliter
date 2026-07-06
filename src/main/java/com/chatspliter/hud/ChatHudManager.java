package com.chatspliter.hud;

import com.chatspliter.config.ChatSpliterConfig;
import com.chatspliter.config.FilterGroup;
import com.chatspliter.screen.GroupConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ChatHudManager {
    private static ChatHudManager INSTANCE;
    private final List<FilteredChatHud> huds = new ArrayList<>();
    private final MinecraftClient client;

    private ChatHudManager() { this.client = MinecraftClient.getInstance(); }

    public static ChatHudManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ChatHudManager();
        return INSTANCE;
    }

    public void initialize() { syncFromConfig(); }

    public void syncFromConfig() {
        List<FilterGroup> groups = ChatSpliterConfig.getInstance().filterGroups;
        // Remove extra HUDs
        while (huds.size() > groups.size())
            huds.remove(huds.size() - 1);
        // Add missing HUDs
        while (huds.size() < groups.size())
            huds.add(new FilteredChatHud(groups.get(huds.size())));
        // Update config references for existing HUDs (preserves message history)
        for (int i = 0; i < huds.size(); i++)
            huds.get(i).replaceConfig(groups.get(i));
    }

    public List<FilteredChatHud> getHuds() { return huds; }

    public void onChatMessage(Text message, int tickCounter) {
        if (!ChatSpliterConfig.getInstance().enabled) return;
        for (FilteredChatHud hud : huds) hud.addMessage(message, tickCounter);
    }

    public void render(DrawContext context, int tickCounter) {
        if (!ChatSpliterConfig.getInstance().enabled || client.player == null) return;
        var w = client.getWindow();
        int sw = w.getScaledWidth(), sh = w.getScaledHeight();
        for (FilteredChatHud hud : huds) {
            hud.render(context, tickCounter, sw, sh,
                    g -> client.setScreen(new GroupConfigScreen(null, g)));
        }
    }

    public void refreshAll() { for (FilteredChatHud h : huds) h.refresh(); }

    // --------------- Drag interaction routing ---------------

    public FilteredChatHud getHudAt(double mx, double my) {
        var w = client.getWindow();
        for (FilteredChatHud h : huds)
            if (h.isMouseOver(mx, my, w.getScaledWidth(), w.getScaledHeight())) return h;
        return null;
    }

    public void onMouseScroll(double mx, double my, double amount) {
        FilteredChatHud h = getHudAt(mx, my);
        if (h != null) h.onScroll(amount);
    }

    public boolean onMouseClick(double mx, double my, int button) {
        if (button != 0) return false;
        FilteredChatHud h = getHudAt(mx, my);
        if (h == null) return false;
        var w = client.getWindow();
        int sw = w.getScaledWidth(), sh = w.getScaledHeight();
        int zone = h.getInteractionZone(mx, my, sw, sh);
        if (zone == 3) {
            // Settings button clicked
            client.setScreen(new GroupConfigScreen(null, h.getConfig()));
            return true;
        }
        if (zone > 0) {
            h.startDrag((int) mx, (int) my, zone, sw, sh);
            return true;
        }
        return false;
    }

    public boolean onMouseDrag(double mx, double my) {
        var w = client.getWindow();
        for (FilteredChatHud h : huds) {
            if (h.isDragging()) {
                h.onDrag((int) mx, (int) my, w.getScaledWidth(), w.getScaledHeight());
                return true;
            }
        }
        return false;
    }

    public void onMouseRelease() {
        for (FilteredChatHud h : huds) h.endDrag();
    }

    public boolean isAnyDragging() {
        for (FilteredChatHud h : huds) if (h.isDragging()) return true;
        return false;
    }
}
