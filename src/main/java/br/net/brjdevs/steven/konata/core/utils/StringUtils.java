package br.net.brjdevs.steven.konata.core.utils;

import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|[^\\s]+");

    public static String[] splitArgs(String input, int size) {
        input = input.trim().replaceAll("  ", " ");
        List<String> results = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(input);
        while (matcher.find()) {
            if (results.size() >= size) {
                String s = input.substring(String.join(" ", results).length());
                if (s.length() > 3 && results.get(results.size() - 1).contains(" ")) s = s.substring(3);
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
        return results.toArray(new String[0]);
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
}
