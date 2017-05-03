package br.net.brjdevs.steven.konata.core.data;

import br.net.brjdevs.steven.konata.core.data.guild.Announces;
import br.net.brjdevs.steven.konata.core.data.guild.CustomCommand;
import br.net.brjdevs.steven.konata.core.data.guild.OldCustomCommand;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

import static com.rethinkdb.RethinkDB.r;

public class DataManager {

    private static Connection conn = null;
    private static DataManager instance;

    public static Connection conn() {
        if (conn == null) {
            conn = r.connection().hostname("localhost").port(28015).connect();
        }
        return conn;
    }

    public static DataManager db() {
        if (instance == null)
            instance = new DataManager();
        return instance;
    }

    public CustomCommand getCustomCommand(String guild, String name) {
        return r.table(CustomCommand.DB_TABLE).get(guild + ":" + name).run(conn(), CustomCommand.class);
    }

    public CustomCommand getCustomCommand(Guild guild, String name) {
        return getCustomCommand(guild.getId(), name);
    }

    public List<CustomCommand> getCustomCommands(String guild) {
        Cursor<CustomCommand> cursor = r.table(CustomCommand.DB_TABLE).filter(cmd -> cmd.g("id").match("^" + guild + ":")).run(conn(), CustomCommand.class);
        return cursor.toList();
    }

    public List<CustomCommand> getCustomCommands(Guild guild) {
        return getCustomCommands(guild.getId());
    }

    public ProfileData getProfile(String userId) {
        ProfileData data = r.table(ProfileData.DB_TABLE).get(userId).run(conn(), ProfileData.class);
        return data != null ? data : new ProfileData(userId);
    }

    public ProfileData getProfile(User user) {
        return getProfile(user.getId());
    }

    private DataCache<GuildData> guilds;
    private DataManager() {
        guilds = new DataCache<>(600000);
    }

    public DataCache<GuildData> getGuilds() {
        return guilds;
    }

    public GuildData getGuild(Guild guild) {
        long id = guild.getIdLong();
        if (guilds.containsKey(id))
            return guilds.getData(id);
        Table table = r.table(GuildData.DB_TABLE);
        GuildData guildData;
        if ((guildData = table.get(guild.getId()).run(conn(), GuildData.class)) != null) {
            guilds.put(id, guildData);
            return guildData;
        } else {
            guildData = new GuildData(guild);
            guilds.put(id, guildData);
            return guildData;

        }
    }

    public Announces getAnnounces(Guild guild) {
        Table table = r.table(Announces.DB_TABLE);
        Announces announces;
        if ((announces = table.get(guild.getId()).run(conn(), Announces.class)) != null) {
            return announces;
        } else {
            announces = new Announces(guild.getId(), null, null,null);
            return announces;

        }
    }
}
