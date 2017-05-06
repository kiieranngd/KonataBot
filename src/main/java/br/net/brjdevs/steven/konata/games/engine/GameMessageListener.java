package br.net.brjdevs.steven.konata.games.engine;

import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.events.EventListener;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GameMessageListener extends EventListener<MessageReceivedEvent> {

    public GameMessageListener() {
        super(MessageReceivedEvent.class);
    }
    @Override
    public void onEvent(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isFake())
            return;
        ProfileData data = ProfileData.of(event.getAuthor());
        AbstractGame abstractGame = AbstractGame.GAMES_RUNNING.get(data.getCurrentGame());
        if (abstractGame == null || !abstractGame.isPrivateAvailable() && !event.isFromType(ChannelType.TEXT))
            return;
        abstractGame.call(event);
    }
}
