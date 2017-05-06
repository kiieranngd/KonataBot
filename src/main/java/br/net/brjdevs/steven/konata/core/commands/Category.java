package br.net.brjdevs.steven.konata.core.commands;

public enum Category {
    FUN("Fun", "\uD83D\uDD79"),
    INFORMATIVE("Informative", "\u2139"),
    ECONOMY("Economy", "\uD83D\uDCB0"),
    MODERATION("Moderation", "\uD83D\uDDA5"),
    MUSIC("Music", "\uD83C\uDFB5"),
    MISCELLANEOUS("Miscellaneous", "\uD83D\uDD2E"),
    BOT_ADMIN("Bot Administrator", "\u2699");
    private String name, emoji;


    Category(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
    public String getName() {
        return name;
    }
}
