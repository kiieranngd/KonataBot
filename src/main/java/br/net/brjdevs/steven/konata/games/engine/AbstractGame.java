package br.net.brjdevs.steven.konata.games.engine;

import br.com.brjdevs.java.snowflakes.Snowflakes;
import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.games.engine.events.GameEvent;
import br.net.brjdevs.steven.konata.games.engine.events.StopEvent;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;

public abstract class AbstractGame {
    public static final Map<GameReference, AbstractGame> GAMES_RUNNING = new HashMap<>();

    protected final GameReference ref;
    protected final long id;
    protected final GamePlayer[] players;
    protected final long channel, startTime;
    protected final boolean isPrivate;
    private final List<GameListener> listeners;

    public AbstractGame(int maxPlayers, MessageChannel messageChannel, User user) {
        this.isPrivate = messageChannel instanceof PrivateChannel;
        if (isPrivate && !isPrivateAvailable())
            throw new IllegalArgumentException("Cannot play " + getName() + " in private messages!");
        this.id = Snowflakes.DISCORD_FACTORY.worker(1, 1).generate();
        this.ref = new GameReference(this);
        this.players = new GamePlayer[maxPlayers];
        this.players[0] = new GamePlayer(user, ref);
        this.channel = messageChannel.getIdLong();
        this.startTime = System.currentTimeMillis();
        this.listeners = new ArrayList<>();
        setup();
        GAMES_RUNNING.put(ref, this);
    }

    public void addListener(GameListener listener) {
        this.listeners.add(listener);
    }

    protected void fireEvent(GameEvent event) {
        listeners.forEach(listener -> listener.onEvent(event));
    }

    public GamePlayer[] getPlayers() {
        return Arrays.stream(players).filter(Objects::nonNull).toArray(GamePlayer[]::new);
    }

    public int getAvailablePlayerSlots() {
        return (int) Arrays.stream(players).filter(Objects::isNull).count();
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public MessageChannel getChannel() {
        return isPrivate ? KonataBot.getInstance().getPrivateChannelById(channel) : KonataBot.getInstance().getTextChannelById(channel);
    }

    public void stop() {
        fireEvent(new StopEvent(this));
    }

    public long getStartTime() {
        return startTime;
    }
    public long getSessionId() {
        return id;
    }
    public abstract void setup();
    public abstract String getName();
    public abstract boolean isPrivateAvailable();
    public abstract boolean isGameOver();
    public abstract boolean isTurn(GamePlayer player);
    public abstract void end(GameEndReason endReason);
    public abstract void call(MessageReceivedEvent event);


    @Override
    public String toString() {
        return "AbstractGame {" +
                "name=" + players.length + "," +
                "maxPlayers=" + players.length + "," +
                "privateAvailable=" + isPrivateAvailable() + "," +
                "private=" + isPrivate + "," +
                "gameOver=" + isGameOver() + "," +
                "sessionId=" + getSessionId() +
                "}";
    }

    public enum GameEndReason {
        VICTORY, DEFEAT, TIE, STOP
    }
}
