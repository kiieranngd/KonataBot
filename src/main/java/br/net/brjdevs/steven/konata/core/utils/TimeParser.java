package br.net.brjdevs.steven.konata.core.utils;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {
    public static String toTextString(long duration) {
        final long years = duration / 31104000000L,
                months = duration / 2592000000L % 12,
                days = duration / 86400000L % 30,
                hours = duration / 3600000L % 24,
                minutes = duration / 60000L % 60,
                seconds = duration / 1000L % 60;

        return StringUtils.replaceLast(StringUtils.replaceLast((years == 0 ? "" : years + " years, ")
                + (months == 0 ? "" : months + " months, ")
                + (days == 0 ? "" : days + " days, ")
                + (hours == 0 ? "" : hours + " hours, ")
                + (minutes == 0 ? "" : minutes + " minutes, ")
                + (seconds == 0 ? "" : seconds + " seconds, "),
                ", ", ""), ",", " and");
    }

    private static final Pattern pattern = Pattern.compile("\\d+?[a-zA-Z]");

    private static Iterable<String> iterate(Matcher matcher) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return matcher.find();
                    }

                    @Override
                    public String next() {
                        return matcher.group();
                    }
                };
            }

            @Override
            public void forEach(Consumer<? super String> action) {
                while (matcher.find()) {
                    action.accept(matcher.group());
                }
            }
        };
    }

    private static long toMillis(String s) {
        s = s.toLowerCase();
        long[] time = {0};
        iterate(pattern.matcher(s)).forEach(string -> {
            String l = string.substring(0, string.length() - 1);
            TimeUnit unit;
            switch (string.charAt(string.length() - 1)) {
                case 's':
                    unit = TimeUnit.SECONDS;
                    break;
                case 'm':
                    unit = TimeUnit.MINUTES;
                    break;
                case 'h':
                    unit = TimeUnit.HOURS;
                    break;
                case 'd':
                    unit = TimeUnit.DAYS;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            time[0] += unit.toMillis(Long.parseLong(l));
        });
        return time[0];
    }
}
