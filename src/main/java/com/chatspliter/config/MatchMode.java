package com.chatspliter.config;

public enum MatchMode {
    ANY("chatspliter.filter_group.match_mode.any"),
    ALL("chatspliter.filter_group.match_mode.all"),
    REGEX("chatspliter.filter_group.match_mode.regex");

    private final String translationKey;

    MatchMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
