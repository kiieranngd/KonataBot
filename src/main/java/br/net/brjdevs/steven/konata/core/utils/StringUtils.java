package br.net.brjdevs.steven.konata.core.utils;

import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class StringUtils {

    private static final String ACTIVE_BLOCK = "\u2588";
    private static final String EMPTY_BLOCK = "\u00AD";

    private static final Pattern PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|[^\\s]+");

    public static String[] splitArgs(String input, int size) {
        input = input.trim();
        List<String> results = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(input.trim());
        while (matcher.find()) {
            if (results.size() >= size - 1) {
                String s = input.substring(String.join(" ", results).length());
                if (s.length() > 3 && results.get(results.size() - 1).contains(" ")) s = s.substring(3).trim();
                results.add(s);
                break;
            }
            if (matcher.group(1) != null) {
                results.add(matcher.group(1).trim());
            } else if (matcher.group(2) != null) {
                results.add(matcher.group(2).trim());
            } else {
                results.add(matcher.group().trim());
            }
        }
        String[] result = new String[size];
        IntStream.range(0, size).forEach(i -> result[i] = getOrDefault(results, i, ""));
        return result;
    }

    private static String getOrDefault(List<String> list, int index, String dString) {
        try {
            return list.get(index);
        } catch (IndexOutOfBoundsException e) {
            return dString;
        }
    }

    public static String toString(User user) {
        return user != null ? user.getName() + "#" + user.getDiscriminator() : "Unknown#0000";
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    public static String parseTime(long time) {
        final long years = time / 31104000000L,
                months = time / 2592000000L % 12,
                days = time / 86400000L % 30,
                hours = time / 3600000L % 24,
                minutes = time / 60000L % 60,
                seconds = time / 1000L % 60;
        return (years == 0 ? "" : decimal(years) + ":")
                + (months == 0 ? "" : decimal(months) + ":")
                + (days == 0 ? "" : decimal(days) + ":")
                + (hours == 0 ? "" : decimal(hours) + ":")
                + (minutes == 0 ? "00" : decimal(minutes)) + ":"
                + (seconds == 0 ? "00" : decimal(seconds));
    }

    public static String decimal(long num) {
        if (num > 9) return String.valueOf(num);
        return "0" + num;
    }

    public static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static String getProgressBar(long l, long total) {
        int activeBlocks = (int) ((float) l / (float) total * 10f);
        StringBuilder builder = new StringBuilder().append(EMPTY_BLOCK);
        for (int i = 0; i < 10; i++) builder.append(activeBlocks >= i ? ACTIVE_BLOCK : ' ');
        return builder.append(EMPTY_BLOCK).toString();
    }

    public static String escapeFormatting(String message) {
        return message.replace("*", "\\*").replace("_", "\\_").replace("~", "\\~").replace("`", "\\`");
    }
}
