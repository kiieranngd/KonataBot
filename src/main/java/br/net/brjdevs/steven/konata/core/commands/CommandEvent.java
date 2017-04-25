package br.net.brjdevs.steven.konata.core.commands;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;

public class CommandEvent {
    private ICommand command;
    private MessageReceivedEvent event;
    private String arguments;

    public CommandEvent(ICommand command, MessageReceivedEvent event, String arguments) {
        this.command = command;
        this.event = event;
        this.arguments = arguments;
    }

    public RestAction<Message> sendMessage(String msg) {
        event.getChannel().sendTyping().complete();
        return event.getChannel().sendMessage(msg);
    }

    public RestAction<Message> sendMessage(Message msg) {
        event.getChannel().sendTyping().complete();
        return event.getChannel().sendMessage(msg);
    }

    public RestAction<Message> sendMessage(MessageEmbed msg) {
        event.getChannel().sendTyping().complete();
        return event.getChannel().sendMessage(msg);
    }

    public ICommand getCommand() {
        return command;
    }

    public boolean isFromType(ChannelType type) {
        return event.isFromType(type);
    }

    public Member getMember() {
        return event.getMember();
    }

    public MessageChannel getChannel() {
        return event.getChannel();
    }

    public User getAuthor() {
        return event.getAuthor();
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public String getArguments() {
        return arguments;
    }

    public JDA getJDA() {
        return event.getJDA();
    }

    public Message getMessage() {
        return event.getMessage();
    }
}
