package net.deanly.solana.sdk.rpc.request.config;

import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.types.Commitment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteAccountsConfig {

    @Json(name = "commitment")
    private Commitment commitment; // Optional: Specifies the commitment level.

    @Json(name = "votePubkey")
    private PublicKey votePubkey; // Optional: Only return results for this validator vote address (base-58 encoded).

    @Json(name = "keepUnstakedDelinquents")
    private Boolean keepUnstakedDelinquents; // Optional: Whether to filter out delinquent validators with no stake.

    @Json(name = "delinquentSlotDistance")
    private UnsignedLong delinquentSlotDistance; // Optional: The slot distance to consider a validator delinquent.
}