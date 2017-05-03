package br.net.brjdevs.steven.konata.cmds.fun;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.Emojis;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegionalCommand {

    private static final Map<String, String> regional = new HashMap<>();

    static {
        regional.put("+", ":heavy_plus_sign:");
        regional.put("-", ":heavy_minus_sign:");
        regional.put("$", ":heavy_dollar_sign:");
        regional.put("#", ":hash:");
        regional.put("*", ":asterisk:");
        regional.put(".", ":record_button:");
        regional.put("0", ":zero:");
        regional.put("1", ":one:");
        regional.put("2", ":two:");
        regional.put("3", ":three:");
        regional.put("4", ":four:");
        regional.put("5", ":five:");
        regional.put("6", ":six:");
        regional.put("7", ":seven:");
        regional.put("8", ":eight:");
        regional.put("9", ":nine:");
        regional.put(" ", "    ");
        regional.put("a", "");
        regional.put("b", "");
        regional.put("c", "");
        regional.put("d", "");
        regional.put("e", "");
        regional.put("f", "");
        regional.put("g", "");
        regional.put("h", "");
        regional.put("i", "");
        regional.put("j", "");
        regional.put("k", "");
        regional.put("l", "");
        regional.put("m", "");
        regional.put("n", "");
        regional.put("o", "");
        regional.put("p", "");
        regional.put("q", "");
        regional.put("r", "");
        regional.put("s", "");
        regional.put("t", "");
        regional.put("u", "");
        regional.put("v", "");
        regional.put("w", "");
        regional.put("x", "");
        regional.put("y", "");
        regional.put("z", "");
    }

    @RegisterCommand
    public static ICommand regional() {
        return new ICommand.Builder()
                .setAliases("regional")
                .setName("Regional Command")
                .setDescription("Make a phrase look cool!")
                .setCategory(Category.FUN)
                .setAction((event) -> {
                    if (event.getArguments().isEmpty()) {
                        event.sendMessage(Stream.of("make sentences look like this".split("")).map(str -> (regional.getOrDefault(str, "\u00ad").isEmpty() ? ":regional_indicator_" + str + ":" : regional.getOrDefault(str, str))).collect(Collectors.joining(" "))).queue();
                    }
                    String[] characters = event.getArguments().toLowerCase().split("");
                    String formatted = Stream.of(characters).map(str -> (regional.getOrDefault(str, "\u00ad").isEmpty() ? ":regional_indicator_" + str + ":" : regional.getOrDefault(str, str))).collect(Collectors.joining(" ")).trim();
                    if (formatted.length() > 2000) {
                        event.sendMessage(Emojis.X + " Output is too long, please reduce your input!").queue();
                        return;
                    }
                    event.sendMessage(formatted).queue();
                })
                .build();
    }
}
