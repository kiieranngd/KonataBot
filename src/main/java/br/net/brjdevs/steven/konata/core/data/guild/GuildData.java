package br.net.brjdevs.steven.konata.core.data.guild;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.data.DataManager;
import br.net.brjdevs.steven.konata.core.data.rethink.DBObject;
import br.net.brjdevs.steven.konata.core.music.AudioLoader;
import br.net.brjdevs.steven.konata.core.permissions.Permissions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static br.net.brjdevs.steven.konata.core.data.DataManager.conn;
import static com.rethinkdb.RethinkDB.r;

public class GuildData implements DBObject {
    public static GuildData of(Guild guild) {
        return DataManager.db().getGuild(guild);
    }

    public static String DB_TABLE = "guilds";

    private String id, customPrefix;
    private long maxSongLength;
    private Map<String, Long> permissions;

    @ConstructorProperties({"id", "customPrefix", "maxSongLength", "permissions"})
    public GuildData(String id, String customPrefix, long maxSongLength, Map<String, String> permissions) {
        this.id = id;
        this.customPrefix = customPrefix;
        this.maxSongLength = maxSongLength;
        this.permissions = permissions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Long.parseLong(entry.getValue())));
    }

    public GuildData(Guild guild) {
        this(guild.getId(), null, AudioLoader.MAX_SONG_LENGTH, new HashMap<>());
    }

    public String getId() {
        return id;
    }

    public String getCustomPrefix() {
        return customPrefix;
    }

    public long getMaxSongLength() {
        return maxSongLength;
    }

    public void setCustomPrefix(String customPrefix) {
        this.customPrefix = customPrefix;
    }

    @JsonIgnore
    public Map<String, Long> getPermissions() {
        return permissions;
    }

    @JsonIgnore
    public long getPermission(Member member) {
        boolean isBotOwner = KonataBot.getInstance().isOwner(member.getUser());
        return permissions.getOrDefault(member.getUser().getId(), isBotOwner ? Permissions.BASE_USER | Permissions.BOT_OWNER : member.isOwner() ? Permissions.BASE_USER | Permissions.GUILD_OWNER : member.hasPermission(Permission.MANAGE_SERVER) ? Permissions.BASE_USER | Permissions.GUILD_MOD : permissions.getOrDefault("default", Permissions.BASE_USER));
    }

    @JsonIgnore
    public boolean setPermission(Member author, Member target, long permsToAdd, long permsToTake) {
        if (author.equals(target) || author.getUser().isBot())
            return false;
        long senderPerm = getPermission(author), targetPerm = getPermission(target);
        if (!Permissions.checkPerms(senderPerm, targetPerm)
                || (senderPerm & (permsToAdd | permsToTake)) != (permsToAdd | permsToTake))
            return false;
        permissions.put(target.getUser().getId(), targetPerm ^ (targetPerm & permsToTake) | permsToAdd);
        return true;
    }

    @JsonIgnore
    public boolean hasPermission(Member member, long perm) {
        return (perm & getPermission(member)) == perm;
    }

    @JsonProperty("permissions")
    public Map<String, String> getPermissionsAsStrings() {
        return permissions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue())));
    }

    public void setMaxSongLength(long maxSongLength) {
        this.maxSongLength = maxSongLength;
    }
    @JsonIgnore
    @Override
    public void save() {
        r.table(DB_TABLE).insert(this).optArg("conflict", "replace").runNoReply(conn());
    }

    @JsonIgnore
    @Override
    public void delete() {
        r.table(DB_TABLE).get(getId()).delete().runNoReply(conn());
    }
}
