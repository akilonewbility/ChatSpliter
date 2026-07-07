package com.chatspliter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.neoforged.fml.loading.FMLPaths;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatSpliterConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("chatspliter.json");

    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("hideMatchedFromMain")
    public boolean hideMatchedFromMain = false;

    @SerializedName("historyMinutes")
    public int historyMinutes = 10;

    @SerializedName("filterGroups")
    public List<FilterGroup> filterGroups = new CopyOnWriteArrayList<>();

    private static ChatSpliterConfig INSTANCE;

    public static ChatSpliterConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ChatSpliterConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                ChatSpliterConfig config = GSON.fromJson(reader, ChatSpliterConfig.class);
                if (config != null) {
                    // Ensure filterGroups is mutable
                    if (config.filterGroups == null) {
                        config.filterGroups = new CopyOnWriteArrayList<>();
                    } else if (!(config.filterGroups instanceof CopyOnWriteArrayList)) {
                        config.filterGroups = new CopyOnWriteArrayList<>(config.filterGroups);
                    }
                    INSTANCE = config;
                    return config;
                }
            } catch (Exception e) {
                System.err.println("[ChatSpliter] Failed to load config: " + e.getMessage());
            }
        }
        ChatSpliterConfig config = new ChatSpliterConfig();
        config.filterGroups = new CopyOnWriteArrayList<>();
        // Add default filter groups
        config.filterGroups.add(createDefaultGroup("Party", "party"));
        config.filterGroups.add(createDefaultGroup("Whisper", "whispers", "whisper to you"));
        config.filterGroups.add(createDefaultGroup("System", "[System]", "[Server]"));
        INSTANCE = config;
        config.save();
        return config;
    }

    private static FilterGroup createDefaultGroup(String name, String... keywords) {
        FilterGroup group = new FilterGroup(name);
        for (String kw : keywords) {
            group.keywords.add(kw);
        }
        return group;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            System.err.println("[ChatSpliter] Failed to save config: " + e.getMessage());
        }
    }

    public void reset() {
        this.enabled = true;
        this.hideMatchedFromMain = false;
        this.filterGroups = new CopyOnWriteArrayList<>();
        this.filterGroups.add(createDefaultGroup("Party", "party"));
        this.filterGroups.add(createDefaultGroup("Whisper", "whispers", "whisper to you"));
        this.filterGroups.add(createDefaultGroup("System", "[System]", "[Server]"));
        save();
    }

    /**
     * Find the first filter group that matches the given message text.
     * Returns null if no group matches.
     */
    public FilterGroup findMatchingGroup(String plainText) {
        for (FilterGroup group : filterGroups) {
            if (group.enabled && group.matches(plainText)) {
                return group;
            }
        }
        return null;
    }
}
