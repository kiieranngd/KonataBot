package br.net.brjdevs.steven.konata.core.data.guild;

import br.net.brjdevs.steven.konata.core.data.DataManager;
import br.net.brjdevs.steven.konata.core.data.rethink.DBObject;
import br.net.brjdevs.steven.konata.core.music.AudioLoader;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.dv8tion.jda.core.entities.Guild;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;

import static com.rethinkdb.RethinkDB.r;
import static br.net.brjdevs.steven.konata.core.data.DataManager.conn;

public class GuildData implements DBObject {

    public static GuildData of(Guild guild) {
        return DataManager.db().getGuild(guild);
    }

    public static String DB_TABLE = "guilds";

    private String id, customPrefix;
    private Map<String, OldCustomCommand> customCommands;
    private long maxSongLength;

    @ConstructorProperties({"id", "customPrefix", "customCommands", "maxSongLength"})
    public GuildData(String id, String customPrefix, Map<String, OldCustomCommand> customCommands, long maxSongLength) {
        this.id = id;
        this.customPrefix = customPrefix;
        this.customCommands = customCommands;
        this.maxSongLength = maxSongLength;
    }

    public GuildData(Guild guild) {
        this(guild.getId(), null, new HashMap<>(), AudioLoader.MAX_SONG_LENGTH);
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

    public Map<String, OldCustomCommand> getCustomCommands() {
        if (customCommands == null)
            customCommands = new HashMap<>();
        return customCommands;
    }

    public void setCustomPrefix(String customPrefix) {
        this.customPrefix = customPrefix;
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
