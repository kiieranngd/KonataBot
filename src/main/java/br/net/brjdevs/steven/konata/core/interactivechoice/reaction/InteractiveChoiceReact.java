package br.net.brjdevs.steven.konata.core.interactivechoice.reaction;

import br.net.brjdevs.steven.konata.core.TaskManager;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InteractiveChoiceReact {
    public static TLongObjectMap<InteractiveChoiceReact> choices = new TLongObjectHashMap<>();

    static {
        TaskManager.startAsyncTask("Interactive Choice Expirator", (service) -> {
            for (long l : choices.keys().clone()) {
                InteractiveChoiceReact i = choices.get(l);
                if (i.waitUntil < System.currentTimeMillis()) {
                    i.getListener().onTimeOut(i);
                    choices.remove(l);
                }
            }
        }, 5);
    }

    public static InteractiveChoiceReact get(User user) {
        return choices.get(user.getIdLong());
    }

    public static void remove(User user) {
        choices.remove(user.getIdLong());
    }

    private final long channelId, waitUntil, messageId;
    private final String[] acceptedResponses;
    private final ICRListener listener;

    private InteractiveChoiceReact(long channelId, long userId, long messageId, long waitUntil, String[] acceptedResponses, ICRListener listener) {
        this.channelId = channelId;
        this.waitUntil = waitUntil;
        this.messageId = messageId;
        this.acceptedResponses = acceptedResponses;
        this.listener = listener;
        choices.put(userId, this);
    }

    public ICRListener getListener() {
        return listener;
    }

    public long getChannelId() {
        return channelId;
    }

    public String[] getAcceptedResponses() {
        return acceptedResponses;
    }
    public long getMessageId() {
        return messageId;
    }
    public static class Builder {
        private BiConsumer<InteractiveChoiceReact, MessageReaction> validResponse, invalidResponse;
        private Consumer<InteractiveChoiceReact> timeout;
        private long channelId, waitUntil, userId, messageId;
        private String[] acceptedResponses;

        public InteractiveChoiceReact.Builder setAcceptedResponses(String... acceptedResponses) {
            this.acceptedResponses = acceptedResponses;
            return this;
        }

        public Builder setMessage(Message message) {
            this.messageId = message.getIdLong();
            return this;
        }

        public InteractiveChoiceReact.Builder setChannel(TextChannel channel) {
            this.channelId = channel.getIdLong();
            return this;
        }

        public InteractiveChoiceReact.Builder expireIn(long millis) {
            this.waitUntil = millis + System.currentTimeMillis();
            return this;
        }

        public InteractiveChoiceReact.Builder onValidResonse(BiConsumer<InteractiveChoiceReact, MessageReaction> validResponse) {
            this.validResponse = validResponse;
            return this;
        }

        public InteractiveChoiceReact.Builder onInvalidResonse(BiConsumer<InteractiveChoiceReact, MessageReaction> invalidResponse) {
            this.invalidResponse = invalidResponse;
            return this;
        }

        public InteractiveChoiceReact.Builder onTimeout(Consumer<InteractiveChoiceReact> timeout) {
            this.timeout = timeout;
            return this;
        }

        public InteractiveChoiceReact.Builder setUser(User user) {
            this.userId = user.getIdLong();
            return this;
        }

        public InteractiveChoiceReact build() {
            return new InteractiveChoiceReact(channelId, userId, messageId, waitUntil, acceptedResponses, new ICRListener() {
                @Override
                public void onValidResponse(InteractiveChoiceReact choice, MessageReaction reaction) {
                    validResponse.accept(choice, reaction);
                }
                @Override
                public void onInvalidResponse(InteractiveChoiceReact choice, MessageReaction reaction) {
                    invalidResponse.accept(choice, reaction);
                }
                @Override
                public void onTimeOut(InteractiveChoiceReact choice) {
                    timeout.accept(choice);
                }
            });
        }
    }
}
