package br.net.brjdevs.steven.konata.games.engine;

import br.net.brjdevs.steven.konata.core.data.DataManager;
import br.net.brjdevs.steven.konata.core.data.user.ProfileData;

public class GamePlayer {
    private String id;

    public GamePlayer(ProfileData data) {
        this.id = data.getId();
    }

    public String getId() {
        return id;
    }

    public ProfileData getProfile() {
        return DataManager.db().getProfile(getId());
    }
}
