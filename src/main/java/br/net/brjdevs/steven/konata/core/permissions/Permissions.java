package br.net.brjdevs.steven.konata.core.permissions;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Permissions {

    public static final long
            RUN_CMD = bits(0),
            RUN_CUSTOM_CMD = bits(1),
            MUSIC = bits(2),
            DJ_OVERRIDE = bits(3),
            CUSTOM_CMDS_INFO = bits(4),
            CUSTOM_CMDS_CREATE = bits(5),
            CUSTOM_CMDS_MANAGE = bits(6),
            POLL_INFO = bits(7),
            POLL_CREATE = bits(8),
            POLL_MANAGE = bits(9),
            BET_CREATE = bits(10),
            BET_INFO = bits(11),
            BET_MANAGE = bits(12),
            GREETINGS_FAREWELLS = bits(13),
            PREFIX_SET = bits(14),
            MANAGE_COMMANDS = bits(15),
            GUILD_OPTS = bits(16),
            PURGE = bits(17),
            PERMSYS_GM = bits(18),
            PERMSYS_GO = bits(19),
            PERMSYS_BO = bits(20),
            EVAL = bits(21),
            BLACKLIST = bits(22),
            STOP_RESET = bits(23);

    public static final long
            BASE_USER = RUN_CMD | RUN_CUSTOM_CMD | MUSIC | CUSTOM_CMDS_INFO | CUSTOM_CMDS_CREATE | POLL_INFO | POLL_CREATE | BET_CREATE | BET_INFO,
            GUILD_MOD = DJ_OVERRIDE | CUSTOM_CMDS_MANAGE |  POLL_MANAGE | MANAGE_COMMANDS | PURGE | PERMSYS_GM | GUILD_OPTS | BET_MANAGE,
            GUILD_OWNER = GUILD_MOD | GREETINGS_FAREWELLS | PREFIX_SET | PERMSYS_GO,
            BOT_OWNER =  GUILD_OWNER | PERMSYS_BO | EVAL | BLACKLIST | STOP_RESET;

    public static Map<String, Long> perms = new HashMap<>();

    static {
        Arrays.stream(Permissions.class.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) //public static final fields only
                .forEach(field -> {
                    try {
                        perms.put(field.getName(), field.getLong(null));
                    } catch (Exception ignored) {
                    }
                });
    }

    public static boolean checkPerms(long senderPerm, long targetPerm) {
        long perms = bits(14, 15, 16);
        senderPerm &= perms;
        targetPerm &= perms; //Select bits 14, 15, 16
        targetPerm = previousPowerOfTwo(roundToPowerOf2(targetPerm));
        senderPerm = previousPowerOfTwo(roundToPowerOf2(senderPerm)); //Get the biggest
        return targetPerm <= senderPerm;
    }


    private static long bits(long... bits) {
        long mask = 0;
        for (long bit : bits) {
            mask |= (long) Math.pow(2, bit);
        }
        return mask;
    }
    public static List<String> toCollection(long userPerms) {
        return perms.entrySet()
                .stream()
                .filter(entry -> (entry.getValue() & userPerms) == entry.getValue())
                .map(Map.Entry::getKey)
                .sorted(String::compareTo).collect(Collectors.toList());
    }

    public static long roundToPowerOf2(long l) {
        long value = l - 1;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        return value + 1;
    }

    public static long previousPowerOfTwo(long value) {
        return value >> 1;
    }
}
