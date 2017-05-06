package br.net.brjdevs.steven.konata.core.economy;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static br.net.brjdevs.steven.konata.core.utils.TLongMapUtils.computeIfAbsent;

public class Looting {
    private static final TLongObjectMap<AtomicLong> LOOTING = new TLongObjectHashMap<>();

    private static Random r = new Random(System.currentTimeMillis());

    public static Looting of(long channelId) {
        return new Looting(computeIfAbsent(LOOTING, channelId, (id) -> new AtomicLong(0)));
    }

    public static Looting of(TextChannel textChannel) {
        return of(textChannel.getIdLong());
    }


    private final AtomicLong coins;

    public Looting(AtomicLong coins) {
        this.coins = coins;
    }

    public long collect() {
        return coins.getAndSet(0);
    }

    public long collect(long amount) {
        return coins.updateAndGet(x -> x - amount);
    }

    public void drop(long coins) {
        this.coins.addAndGet(coins);
    }

    public boolean drop(int coins, int weight) {
        boolean doDrop = r.nextInt(weight) == 0;
        if (doDrop) drop(coins);
        return doDrop;
    }

    public long size() {
        return coins.longValue();
    }
}
