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

    @SerializedName("maxLines")
    public int maxLines = 100;

    @SerializedName("showTitle")
    public boolean showTitle = true;

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
        copy.maxLines = this.maxLines;
        copy.showTitle = this.showTitle;
        return copy;
    }

    public boolean matches(String plainText) {
        if (keywords.isEmpty()) return false;

        return switch (matchMode) {
            case ANY -> keywords.stream().anyMatch(k -> plainText.toLowerCase().contains(k.toLowerCase()));
            case ALL -> keywords.stream().allMatch(k -> plainText.toLowerCase().contains(k.toLowerCase()));
            case REGEX -> {
                try {
                    yield keywords.stream().anyMatch(k -> plainText.matches(k));
                } catch (Exception e) {
                    yield false;
                }
            }
        };
    }
}
