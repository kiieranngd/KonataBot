package br.net.brjdevs.steven.konata.core.data.guild;

import br.net.brjdevs.steven.konata.core.data.rethink.DBObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static br.net.brjdevs.steven.konata.core.data.DataManager.conn;
import static com.rethinkdb.RethinkDB.r;

public class CustomCommand implements DBObject {
    private static final Random random = new Random();
    public static final String DB_TABLE = "customcmds";

    private final String id, creator;
    private final List<String> answers;
    private final long creationDate;

    @ConstructorProperties({"id", "creator", "answers", "creationDate"})
    public CustomCommand(String id, String creator, List<String> answers, long creationDate) {
        this.id = id;
        this.creator = creator;
        this.answers = answers;
        this.creationDate = creationDate;
    }

    public CustomCommand(Guild guild, Member creator, String name, String answer) {
        this.id = guild.getId() + ":" + name;
        this.creator = creator.getUser().getId();
        this.answers = new ArrayList<>(Collections.singletonList(answer));
        this.creationDate = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    @JsonProperty("creator")
    public String getCreatorId() {
        return creator;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public long getCreationDate() {
        return creationDate;
    }

    @JsonIgnore
    public String getName() {
        return id.split(":")[1];
    }

    @JsonIgnore
    public String getRandomAnswer() {
        return getAnswers().get(random.nextInt(getAnswers().size()));
    }

    @Override
    public void save() {
        r.table(DB_TABLE).insert(this).optArg("conflict", "replace").runNoReply(conn());
    }

    @Override
    public void delete() {
        r.table(DB_TABLE).get(id).delete().runNoReply(conn());
    }
}
