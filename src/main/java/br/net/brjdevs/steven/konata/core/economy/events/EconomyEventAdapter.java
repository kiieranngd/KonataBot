package br.net.brjdevs.steven.konata.core.economy.events;

public class EconomyEventAdapter implements EconomyEventListener {
    @Override
    public void onEvent(Event event) {
        if (event instanceof RobberyEvent)
            onRobbery(((RobberyEvent) event));
        else if (event instanceof ExperienceFoundEvent)
            onExperienceFound(((ExperienceFoundEvent) event));
        else if (event instanceof CoinsFoundEvent)
            onCoinsFound(((CoinsFoundEvent) event));
    }

    public void onRobbery(RobberyEvent event) {}
    public void onExperienceFound(ExperienceFoundEvent event) {}
    public void onCoinsFound(CoinsFoundEvent event) {}
}
