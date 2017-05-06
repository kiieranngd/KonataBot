package br.net.brjdevs.steven.konata.core.data.guild;

import br.net.brjdevs.steven.konata.core.data.rethink.DBObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.core.entities.TextChannel;

import java.beans.ConstructorProperties;

import static br.net.brjdevs.steven.konata.core.data.DataManager.conn;
import static com.rethinkdb.RethinkDB.r;

public class Announces implements DBObject {

    public static final String DB_TABLE = "announces";

    private String id, greeting, farewell, channel;

    @ConstructorProperties({"id", "greeting", "farewell", "channel"})
    public Announces(String id, String greeting, String farewell, String channel) {
        this.id = id;
        this.greeting = greeting;
        this.farewell = farewell;
        this.channel = channel;
    }

    public String getFarewell() {
        return farewell;
    }
    public String getGreeting() {
        return greeting;
    }
    public String getChannel() {
        return channel;
    }
    public String getId() {
        return id;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }
    public void setFarewell(String farewell) {
        this.farewell = farewell;
    }
    public void setGreeting(String greeting) {
        this.greeting = greeting;
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
