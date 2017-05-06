package br.net.brjdevs.steven.konata.core.snow64;

import com.google.common.primitives.Longs;

import java.util.Base64;

public class Snow64Generator {
    private final long sequenceBits = 12L, sequenceMask = ~(-1L << sequenceBits), workerId, workerIdBits = 5L, timestampLeftShift = sequenceBits + workerIdBits, workerIdShift = sequenceBits;
    private long lastTimestamp = -1L, sequence = 0L;

    public Snow64Generator(long workerId) {
        long maxWorkerId = ~(-1L << workerIdBits);
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("Worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        this.workerId = workerId;
    }

    public String nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        synchronized (this) {
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            long twepoch = 1490447852884L;
            long snowflake = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
            return Base64.getEncoder().encodeToString(Longs.toByteArray((snowflake))).replace('/', '-').replace('=', ' ').trim();
        }
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
