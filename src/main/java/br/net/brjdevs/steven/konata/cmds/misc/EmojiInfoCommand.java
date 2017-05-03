package br.net.brjdevs.steven.konata.cmds.misc;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;

public class EmojiInfoCommand {

    @RegisterCommand
    public static ICommand emojiInfo() {
        return new ICommand.Builder()
                .setCategory(Category.MISCELLANEOUS)
                .setAliases("emoji", "character")
                .setName("Emoji/Character Info Command")
                .setDescription("Returns emoji or character info.")
                .setAction((event) -> {
                    if (event.getArguments().isEmpty()) {
                        event.sendMessage("You can use this command to db unicodes, just tell me an emoji or character :eyes:").queue();
                        return;
                    }
                    String unicode = toUnicode(event.getArguments());
                    event.sendMessage("[\u2139] Unicode converter:\n" + unicode).queue();
                })
                .build();
    }

    public static String toUnicode(String emoji) {
        StringBuilder builder = new StringBuilder();
        emoji.codePoints().forEachOrdered(code -> {
            char[] chrs = Character.toChars(code);
            String hex = Integer.toHexString(code);
            while (hex.length() < 4)
                hex = "0" + hex;
            builder.append("\n`\\u").append(hex).append("`   ");
            if(chrs.length>1)
            {
                String hex1 = Integer.toHexString(chrs[0]).toUpperCase();
                String hex2 = Integer.toHexString(chrs[1]).toUpperCase();
                while(hex1.length()<4)
                    hex1 = "0" + hex1;
                while(hex2.length()<4)
                    hex2 = "0" + hex2;
                builder.append("- `\\u").append(hex1).append("\\u").append(hex2).append("`   ");
            }
            builder.append(String.valueOf(chrs)).append("   _").append(Character.getName(code)).append("_");

        });
        return builder.toString();
    }
}
