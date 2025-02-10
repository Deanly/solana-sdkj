package net.deanly.solana.sdk.types;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class SubscriptionId implements Comparable<SubscriptionId> {
    private final long id;

    public SubscriptionId(long id) {
        this.id = id;
    }

    public long getValue() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public static SubscriptionId of(long id) {
        return new SubscriptionId(id);
    }

    @Override
    public int compareTo(SubscriptionId o) {
        return Long.compare(this.id, o.id);
    }
}
