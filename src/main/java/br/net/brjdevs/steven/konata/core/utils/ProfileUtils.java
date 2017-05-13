package br.net.brjdevs.steven.konata.core.utils;

import br.net.brjdevs.steven.konata.core.data.user.ProfileData;

public class ProfileUtils {
    public static void addExperience(ProfileData data, long experience) {
        if (data.getExperience() + experience < 0) {
            return;
        }
        data.setExperience(data.getExperience() + experience);
        if (data.getExperience() >= expForNextLevel(data.getLevel())) {
            data.setExperience(data.getExperience() - expForNextLevel(data.getLevel()));
            data.setLevel(data.getLevel() + 1);
            //getRegisteredListeners().forEach(listener -> listener.onLevelUp(this));
        }
    }

    public static void takeExperience(ProfileData data, long experience) {
        long total = data.getExperience() - experience;
        if (total >= 0 || data.getLevel() > 1)
            data.setExperience(data.getExperience() - experience);
        if (data.getExperience() <= 0 && data.getLevel() > 1) {
            data.setExperience(expForNextLevel(data.getLevel() - 1) + data.getExperience());
            data.setLevel(data.getLevel() - 1);
            //getRegisteredListeners().forEach(listener -> listener.onLevelDown(this));
        }
    }

    public static long expForNextLevel(long level) {
        level++;
        double expCalculate = level * Math.log10(level) * 250;
        long expRequired = Math.round(expCalculate);
        if (expCalculate - expRequired > 0) expRequired++;
        return expRequired;
    }

    public static boolean addCoins(ProfileData data, long coins) {
        if (data.getCoins() + coins < 0)
            return false;
        data.setCoins(data.getCoins() + coins);
        return true;
    }

    public static boolean takeCoins(ProfileData data, long coinsToTake) {
        if (coinsToTake > data.getCoins())
            return false;
        data.setCoins(data.getCoins() - coinsToTake);
        return true;
    }
}
