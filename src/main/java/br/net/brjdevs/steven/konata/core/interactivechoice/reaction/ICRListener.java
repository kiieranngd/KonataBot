package br.net.brjdevs.steven.konata.core.interactivechoice.reaction;

import net.dv8tion.jda.core.entities.MessageReaction;

public interface ICRListener {
    void onValidResponse(InteractiveChoiceReact choice, MessageReaction reaction);
    void onInvalidResponse(InteractiveChoiceReact choice, MessageReaction reaction);
    void onTimeOut(InteractiveChoiceReact choice);
}
