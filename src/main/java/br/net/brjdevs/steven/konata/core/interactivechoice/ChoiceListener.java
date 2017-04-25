package br.net.brjdevs.steven.konata.core.interactivechoice;

import br.net.brjdevs.steven.konata.core.events.EventListener;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

public class ChoiceListener extends EventListener<GuildMessageReceivedEvent> {

    public ChoiceListener() {
        super(GuildMessageReceivedEvent.class);
    }

    @Override
    public void onEvent(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isFake())
            return;
        InteractiveChoice choice = InteractiveChoice.get(event.getAuthor());
        if (choice == null || choice.getChannelId() != event.getChannel().getIdLong()) return;

        String input = event.getMessage().getRawContent();
        if (Arrays.stream(choice.getAcceptedResponses()).anyMatch(x -> x.equals(input)))
            choice.getListener().onValidResponse(choice, input);
        else
            choice.getListener().onInvalidResponse(choice, input);
        InteractiveChoice.remove(event.getAuthor());
    }
}
