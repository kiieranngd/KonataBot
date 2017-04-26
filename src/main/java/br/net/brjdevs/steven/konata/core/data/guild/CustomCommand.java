package br.net.brjdevs.steven.konata.core.data.guild;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.dv8tion.jda.core.entities.User;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CustomCommand {
    private static final Random r = new Random();

    private String name;
    private List<String> answers;
    private long creator, creationDate;

    @ConstructorProperties({"name", "answers", "creator", "creationDate"})
    public CustomCommand(String name, List<String> answers, long creator, long creationDate) {
        this.name = name;
        this.answers = answers;
        this.creator = creator;
        this.creationDate = creationDate;
    }

    public CustomCommand(String name, String firstAnswer, User creator) {
        this.name = name;
        this.answers = new ArrayList<>(Arrays.asList(firstAnswer));
        this.creator = creator.getIdLong();
        this.creationDate = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public long getCreator() {
        return creator;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setName(String name) {
        this.name = name;
    }
    @JsonIgnore
    public String getRandomAnswer() {
        return getAnswers().get(r.nextInt(getAnswers().size()));
    }
}