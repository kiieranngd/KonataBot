package br.net.brjdevs.steven.konata.core.data.user;

import br.net.brjdevs.steven.konata.core.data.DataManager;
import br.net.brjdevs.steven.konata.core.data.rethink.DBObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.beans.ConstructorProperties;

import static br.net.brjdevs.steven.konata.core.data.DataManager.conn;
import static com.rethinkdb.RethinkDB.r;

public class ProfileData implements DBObject {

    public static ProfileData of(User user) {
        return DataManager.db().getProfile(user);
    }

    public static final String DB_TABLE = "profiles";

    private final String id;
    private long experience, level, coins, lastDaily, reputation;
    private String rank;

    @ConstructorProperties({"id", "experience", "level", "coins", "lastDaily", "reputation", "rank"})
    public ProfileData(String id, long experience, long level, long coins, long lastDaily, long reputation, String rank) {
        this.id = id;
        this.experience = experience;
        this.level = level;
        this.coins = coins;
        this.lastDaily = lastDaily;
        this.reputation = reputation;
        this.rank = rank;
    }

    public ProfileData(User user) {
        this(user.getId());
    }

    public ProfileData(String userId) {
        this(userId, 0, 1, 0, 0, 0, "ROOKIE");
    }

    public String getId() {
        return id;
    }
    public long getLevel() {
        return level;
    }
    public long getCoins() {
        return coins;
    }
    public long getExperience() {
        return experience;
    }
    public long getLastDaily() {
        return lastDaily;
    }
    public long getReputation() {
        return reputation;
    }
    @JsonProperty("rank")
    public String getRankName() {
        return rank;
    }
    @JsonIgnore
    public Rank getRank() {
        return Rank.valueOf(rank);
    }
    public void setCoins(long coins) {
        this.coins = coins;
    }
    public void setExperience(long experience) {
        this.experience = experience;
    }
    public void setLastDaily(long lastDaily) {
        this.lastDaily = lastDaily;
    }
    public void setLevel(long level) {
        this.level = level;
    }
    @JsonProperty("rank")
    public void setRankName(String name) {
        this.rank = name;
    }
    @JsonIgnore
    public void setRank(Rank rank) {
        this.rank = rank.name();
    }
    @JsonIgnore
    public void addReputation() {
        reputation++;
    }
    @JsonIgnore
    public void takeReputation() {
        reputation--;
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

    public enum Rank {
        ROOKIE(0, "#5838D6", 0),
        BEGINNER(5, "#38C9D6", 100),
        TALENTED(10, "#8438D6", 150),
        SKILLED(20, "#4EE07D", 500),
        INTERMEDIATE(35, "#93DA38", 1000),
        SKILLFUL(40, "#C0DA38", 1500),
        EXPERIENCED(50, "#DCF80C", 6000),
        ADVANCED(70, "#F8C90C", 10000),
        SENIOR(85, "#FFAD00", 15000),
        EXPERT(100, "#DB2121", 100000);
        private static Rank[] vals = values();
        private final int level;
        private final String hex;
        private long cost;

        Rank(int i, String hex, long cost) {
            this.level = i;
            this.hex = hex;
            this.cost = cost;
        }

        public int getLevel() {
            return level;
        }

        public Color getColor() {
            return Color.decode(hex);
        }

        public long getCost() {
            return cost;
        }

        public Rank next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }

        public Rank previous() {
            if (this.equals(ROOKIE)) return ROOKIE;
            return vals[(this.ordinal() - 1) % vals.length];
        }
    }
}
