package br.net.brjdevs.steven.konata.core.commands;

public enum Category {
    FUN("Fun", ""),
    INFORMATIVE("Informative", ""),
    BOT_ADMIN("Bot Administrator", ""),
    GUILD_ADMIN("Guild Administrator", ""),
    MUSIC("Music", "");
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
