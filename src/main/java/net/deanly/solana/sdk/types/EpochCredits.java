package net.deanly.solana.sdk.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EpochCredits {
    private long epoch;
    private long credits;
    private long previousCredits;
}
