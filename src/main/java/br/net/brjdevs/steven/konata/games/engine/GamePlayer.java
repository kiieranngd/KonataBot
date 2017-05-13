package br.net.brjdevs.steven.konata.games.engine;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.utils.TLongMapUtils;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.entities.User;

import java.util.Objects;

public class GamePlayer {

    private static final TLongObjectMap<GamePlayer> PLAYERS = new TLongObjectHashMap<>();

    public static GamePlayer of(User user) {
        GamePlayer player = TLongMapUtils.getOrDefault(PLAYERS, user.getIdLong(), null);
        if (player != null && player.getGame().toGame() == null) {
            player.destroy();
            return null;
        }
        return player;
    }

    private final GameReference ref;
    private final long id;

    public GamePlayer(User user, GameReference ref) {
        Objects.requireNonNull(ref);
        this.id = user.getIdLong();
        this.ref = ref;
        PLAYERS.put(id, this);
    }

    public GameReference getGame() {
        return ref;
    }

    public long getId() {
        return id;
    }

    public void destroy() {
        PLAYERS.remove(id);
    }

    public User getUser() {
        return KonataBot.getInstance().getUserById(id);
    }
}
