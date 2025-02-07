package net.deanly.solana.sdk.types;

import lombok.*;

@Getter @Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ValidatorIdentityInfo {
    private Integer numberOfLeaderSlots;
    private Integer numberOfBlocksProduced;
}
