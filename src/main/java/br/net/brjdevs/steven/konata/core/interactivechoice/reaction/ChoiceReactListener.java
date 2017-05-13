package br.net.brjdevs.steven.konata.core.interactivechoice.reaction;

import br.net.brjdevs.steven.konata.core.events.EventListener;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

import java.util.Arrays;

public class ChoiceReactListener extends EventListener<MessageReactionAddEvent> {
    public ChoiceReactListener() {
        super(MessageReactionAddEvent.class);
    }
    @Override
    public void onEvent(MessageReactionAddEvent event) {
        if (event.getUser().isBot() || event.getUser().isFake())
            return;
        InteractiveChoiceReact icr = InteractiveChoiceReact.get(event.getUser());
        if (icr == null || icr.getMessageId() != event.getMessageIdLong())
            return;
        String name = event.getReaction().getEmote().getName();
        if (Arrays.stream(icr.getAcceptedResponses()).anyMatch(x -> x.equals(name)))
            icr.getListener().onValidResponse(icr, event.getReaction());
        else
            icr.getListener().onInvalidResponse(icr, event.getReaction());
        InteractiveChoiceReact.remove(event.getUser());
    }
}
