package net.deanly.solana.sdk.types;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EpochCredits {
    /**
     * Represents the current epoch in the system or process.
     * This value is typically a long integer that indicates the sequential
     * position or time-related identifier for the epoch.
     */
    private long epoch; // element 1

    /**
     * Represents the total number of credits accumulated in the current epoch.
     * It is used to track the progress or rewards earned during this epoch.
     */
    private long credits; // element 2

    /**
     * Represents the number of credits that were accumulated up to the previous epoch.
     * Useful for calculating the incremental credits earned during the current epoch.
     */
    private long previousCredits; // element 3

    public static EpochCredits of(long epoch, long credits, long previousCredits) {
        return new EpochCredits(epoch, credits, previousCredits);
    }
}
