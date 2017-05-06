package br.net.brjdevs.steven.konata.core.utils;

import br.net.brjdevs.steven.konata.KonataBot;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;

public class DiscordUtils {

    public static boolean hasRole(Member member, String role, Permission... orElse) {
        return KonataBot.getInstance().isOwner(member.getUser()) || member.getRoles().stream().anyMatch(x -> x.getName().equalsIgnoreCase(role)) || orElse.length > 0 && member.hasPermission(orElse) || member.hasPermission(Permission.ADMINISTRATOR) || member.isOwner();
    }

    public static boolean isDJ(Member member) {
        return hasRole(member, "DJ", Permission.MANAGE_SERVER) || isBotCommander(member);
    }

    public static boolean isBotCommander(Member member) {
        return hasRole(member, "Bot Commander", Permission.MANAGE_SERVER);
    }
}
