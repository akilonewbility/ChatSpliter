package com.chatspliter.config;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class FilterGroup {
    @SerializedName("name")
    public String name = "New Group";

    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("keywords")
    public List<String> keywords = new ArrayList<>();

    @SerializedName("matchMode")
    public MatchMode matchMode = MatchMode.ANY;

    // Position & Size
    @SerializedName("x")
    public int x = 2;

    @SerializedName("y")
    public int y = 2;

    @SerializedName("width")
    public int width = 320;

    @SerializedName("height")
    public int height = 180;

    // Chat display settings (mirror vanilla options)
    @SerializedName("scale")
    public double scale = 1.0;

    @SerializedName("displayTime")
    public double displayTime = 15.0;

    @SerializedName("fadeTime")
    public double fadeTime = 10.0;

    @SerializedName("opacity")
    public double opacity = 0.5;

    @SerializedName("textOpacity")
    public double textOpacity = 1.0;

    @SerializedName("lineSpacing")
    public int lineSpacing = 0;

    @SerializedName("textColor")
    public int textColor = 0xFFFFFF;

    @SerializedName("backgroundColor")
    public int backgroundColor = 0x000000;

    @SerializedName("showTimestamp")
    public boolean showTimestamp = false;

    @SerializedName("maxHistory")
    public int maxHistory = 200;

    @SerializedName("showTitle")
    public boolean showTitle = true;

    @SerializedName("textAlign")
    public TextAlign textAlign = TextAlign.LEFT;

    @SerializedName("scrollDir")
    public ScrollDir scrollDir = ScrollDir.BOTTOM_UP;

    @SerializedName("caseSensitive")
    public boolean caseSensitive = false;

    public enum TextAlign { LEFT, RIGHT }
    public enum ScrollDir { BOTTOM_UP, TOP_DOWN, TOP_ANCHORED }

    public FilterGroup() {
    }

    public FilterGroup(String name) {
        this.name = name;
    }

    public FilterGroup copy() {
        FilterGroup copy = new FilterGroup();
        copy.name = this.name;
        copy.enabled = this.enabled;
        copy.keywords = new ArrayList<>(this.keywords);
        copy.matchMode = this.matchMode;
        copy.x = this.x;
        copy.y = this.y;
        copy.width = this.width;
        copy.height = this.height;
        copy.scale = this.scale;
        copy.displayTime = this.displayTime;
        copy.fadeTime = this.fadeTime;
        copy.opacity = this.opacity;
        copy.textOpacity = this.textOpacity;
        copy.lineSpacing = this.lineSpacing;
        copy.textColor = this.textColor;
        copy.backgroundColor = this.backgroundColor;
        copy.showTimestamp = this.showTimestamp;
        copy.maxHistory = this.maxHistory;
        copy.showTitle = this.showTitle;
        copy.textAlign = this.textAlign;
        copy.scrollDir = this.scrollDir;
        copy.caseSensitive = this.caseSensitive;
        return copy;
    }

    public boolean matches(String plainText) {
        if (keywords.isEmpty()) return false;

        String lower = caseSensitive ? plainText : plainText.toLowerCase();
        List<String> includes = new ArrayList<>();
        List<String> excludes = new ArrayList<>();

        for (String kw : keywords) {
            String t = kw.trim();
            if (t.isEmpty()) continue;
            if (t.startsWith("not/")) {
                excludes.add(caseSensitive ? t.substring(4) : t.substring(4).toLowerCase());
            } else if (t.startsWith("!/")) {
                excludes.add(caseSensitive ? t.substring(2) : t.substring(2).toLowerCase());
            } else {
                includes.add(caseSensitive ? t : t.toLowerCase());
            }
        }

        // Exclusions checked first — any match immediately rejects
        for (String ex : excludes) {
            if (lower.contains(ex)) return false;
        }

        // No includes = match everything (only exclusions act as filter)
        if (includes.isEmpty()) return true;

        return switch (matchMode) {
            case ANY -> includes.stream().anyMatch(lower::contains);
            case ALL -> includes.stream().allMatch(lower::contains);
            case REGEX -> {
                try {
                    yield includes.stream().anyMatch(plainText::matches);
                } catch (Exception e) {
                    yield false;
                }
            }
        };
    }
}
