package net.deanly.solana.sdk.types;

import com.squareup.moshi.Json;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TokenBalance {
    /// Index of the account in which the token balance is provided for.
    @Json(name = "accountIndex")
    private Integer accountIndex;

    /// Pubkey of the token's mint.
    @Json(name = "mint")
    private String mint;

    /// Pubkey of token balance's owner.
    @Json(name = "owner")
    private String owner;

    @Json(name = "uiTokenAmount")
    private UiTokenAmount uiTokenAmount;

}