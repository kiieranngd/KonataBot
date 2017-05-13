package br.net.brjdevs.steven.konata;

import br.net.brjdevs.steven.konata.core.data.Config;
import br.net.brjdevs.steven.konata.core.events.EventManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

public class Shard {
    private JDA instance;
    private int id, total;
    private EventManager eventManager;
    private long started;

    public Shard(int shardId, int shardTotal) throws LoginException, InterruptedException {
        this.id = shardId;
        this.total = shardTotal;
        this.eventManager = new EventManager(this);
        restartJDA();
    }

    public void restartJDA() throws LoginException, InterruptedException {
        Config config = KonataBot.getInstance().getConfig();
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        if (total > 1)
        builder.useSharding(id, total);
        builder.setToken(config.token);
        if (!config.game.isEmpty())
            builder.setGame(Game.of(config.game, config.streamUrl));
        builder.setEventManager(eventManager);
        if (config.corePoolSize > 2)
            builder.setCorePoolSize(config.corePoolSize);

        while (true) {
            try {
                this.instance = builder.buildBlocking();
                break;
            } catch (RateLimitedException e) {
                Thread.sleep(e.getRetryAfter());
            }
        }
        this.started = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public int getShardTotal() {
        return total;
    }

    public JDA getJDA() {
        return instance;
    }

    public long getUptime() {
        return started;
    }

    public EventManager getEventManager() {
        return eventManager;
    }
}
