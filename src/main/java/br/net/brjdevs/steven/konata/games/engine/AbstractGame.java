package br.net.brjdevs.steven.konata.games.engine;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.snow64.Snow64Generator;
import br.net.brjdevs.steven.konata.games.engine.events.GameEvent;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;

public abstract class AbstractGame {
    public static final Map<String, AbstractGame> GAMES_RUNNING = new HashMap<>();
    public static final Snow64Generator SNOW_64_GENERATOR = new Snow64Generator(1);

    protected final String id;
    protected final GamePlayer[] players;
    protected final long channel, startTime;
    protected final boolean isPrivate;
    private final List<GameListener> listeners;

    public AbstractGame(int maxPlayers, MessageChannel messageChannel, ProfileData data) {
        this.isPrivate = messageChannel instanceof PrivateChannel;
        if (isPrivate && !isPrivateAvailable())
            throw new IllegalArgumentException("Cannot play " + getName() + " in private messages!");
        this.id = SNOW_64_GENERATOR.nextId();
        this.players = new GamePlayer[maxPlayers];
        this.players[0] = new GamePlayer(data);
        data.setCurrentGame(id);
        this.channel = messageChannel.getIdLong();
        this.startTime = System.currentTimeMillis();
        this.listeners = new ArrayList<>();
        setup();
        GAMES_RUNNING.put(getSessionId(), this);
    }

    public void addListener(GameListener listener) {
        this.listeners.add(listener);
    }

    public void fireEvent(GameEvent event) {
        listeners.forEach(listener -> listener.onEvent(event));
    }

    public GamePlayer[] getPlayers() {
        return Arrays.stream(players).filter(Objects::nonNull).toArray(GamePlayer[]::new);
    }

    public int getAvailablePlayerSlots() {
        return (int) Arrays.stream(players).filter(Objects::isNull).count();
    }

    public MessageChannel getChannel() {
        return isPrivate ? KonataBot.getInstance().getPrivateChannelById(channel) : KonataBot.getInstance().getTextChannelById(channel);
    }

    public long getStartTime() {
        return startTime;
    }
    public String getSessionId() {
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
