package br.net.brjdevs.steven.konata.core.interactivechoice;

import br.net.brjdevs.steven.konata.core.TaskManager;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InteractiveChoice {
    private static TLongObjectMap<InteractiveChoice> interactiveChoices = new TLongObjectHashMap<>();

    static {
        TaskManager.startAsyncTask("Interactive Choice Expirator", (service) -> {
            for (long l : interactiveChoices.keys()) {
                InteractiveChoice i = interactiveChoices.get(l);
                if (i.waitUntil < System.currentTimeMillis()) {

                }
            }
        }, 5);
    }

    public static InteractiveChoice get(User user) {
        return interactiveChoices.get(user.getIdLong());
    }

    public static void remove(User user) {
        interactiveChoices.remove(user.getIdLong());
    }

    private final long channelId, waitUntil;
    private final String[] acceptedResponses;
    private final ICListener listener;

    private InteractiveChoice(long channelId, long userId, long waitUntil, String[] acceptedResponses, ICListener listener) {
        this.channelId = channelId;
        this.waitUntil = waitUntil;
        this.acceptedResponses = acceptedResponses;
        this.listener = listener;
        interactiveChoices.put(userId, this);
    }

    public ICListener getListener() {
        return listener;
    }

    public long getChannelId() {
        return channelId;
    }

    public String[] getAcceptedResponses() {
        return acceptedResponses;
    }
    public static class Builder {
        private BiConsumer<InteractiveChoice, String> validResponse, invalidResponse;
        private Consumer<InteractiveChoice> timeout;
        private long channelId, waitUntil, userId;
        private String[] acceptedResponses;

        public Builder setAcceptedResponses(String... acceptedResponses) {
            this.acceptedResponses = acceptedResponses;
            return this;
        }

        public Builder setChannel(TextChannel channel) {
            this.channelId = channel.getIdLong();
            return this;
        }

        public Builder expireIn(long millis) {
            this.waitUntil = millis + System.currentTimeMillis();
            return this;
        }

        public Builder onValidResonse(BiConsumer<InteractiveChoice, String> validResponse) {
            this.validResponse = validResponse;
            return this;
        }

        public Builder onInvalidResonse(BiConsumer<InteractiveChoice, String> invalidResponse) {
            this.invalidResponse = invalidResponse;
            return this;
        }

        public Builder onTimeout(Consumer<InteractiveChoice> timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setUser(User user) {
            this.userId = user.getIdLong();
            return this;
        }

        public InteractiveChoice build() {
            return new InteractiveChoice(channelId, userId, waitUntil, acceptedResponses, new ICListener() {
                @Override
                public void onValidResponse(InteractiveChoice choice, String response) {
                    validResponse.accept(choice, response);
                }
                @Override
                public void onInvalidResponse(InteractiveChoice choice, String response) {
                    invalidResponse.accept(choice, response);
                }
                @Override
                public void onTimeOut(InteractiveChoice choice) {
                    timeout.accept(choice);
                }
            });
        }
    }
}
