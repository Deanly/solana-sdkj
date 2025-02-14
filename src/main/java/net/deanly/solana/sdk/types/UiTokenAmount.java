package net.deanly.solana.sdk.types;

import com.squareup.moshi.Json;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UiTokenAmount {
    /// Raw amount of tokens as a string, ignoring decimals.
    @Json(name = "amount")
    private String amount;

    /// Number of decimals configured for token's mint.
    @Json(name = "decimals")
    private Integer decimals;

    /// Token amount as a float, accounting for decimals. DEPRECATED
    @Deprecated
    @Json(name = "uiAmount")
    private Float uiAmount;

    /// Token amount as a string, accounting for decimals.
    @Json(name = "uiAmountString")
    private String uiAmountString;
}
