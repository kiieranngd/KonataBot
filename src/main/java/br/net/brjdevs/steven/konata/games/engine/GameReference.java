package br.net.brjdevs.steven.konata.games.engine;

public class GameReference {
    private final long id;

    public GameReference(AbstractGame game) {
        this.id = game.getSessionId();
    }

    public long getId() {
        return id;
    }

    public <T extends AbstractGame> T toGame(Class<T> clazz) {
        return clazz.cast(AbstractGame.GAMES_RUNNING.get(this));
    }

    public AbstractGame toGame() {
        return AbstractGame.GAMES_RUNNING.get(this);
    }

    public boolean isReferenceFor(Class<? extends AbstractGame> clazz) {
        return clazz.isInstance(AbstractGame.GAMES_RUNNING.get(this));
    }
}
