package br.net.brjdevs.steven.konata.core.data;

import br.net.brjdevs.steven.konata.core.data.guild.Announces;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import net.dv8tion.jda.core.entities.Guild;

import static com.rethinkdb.RethinkDB.r;

public class DataManager {

    private static Connection conn = null;

    public static Connection conn() {
        if (conn == null) {
            conn = r.connection().hostname("localhost").port(28015).connect();
        }
        return conn;
    }

    private DataCache<GuildData> guilds;
    private ObjectMapper mapper;

    public DataManager() {
        mapper = new ObjectMapper();
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
        if ((guildData = mapper.convertValue(table.get(guild.getId()).run(conn()), GuildData.class)) != null) {
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
        if ((announces = mapper.convertValue(table.get(guild.getId()).run(conn()), Announces.class)) != null) {
            return announces;
        } else {
            announces = new Announces(guild.getId(), null, null,null);
            return announces;

        }
    }
}
