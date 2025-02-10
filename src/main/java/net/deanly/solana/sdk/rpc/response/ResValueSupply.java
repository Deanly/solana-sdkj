package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.types.guava.UnsignedLong;
import net.deanly.solana.sdk.crypto.PublicKey;

import java.util.List;

@Getter
@ToString
public class ResValueSupply {

    @Json(name = "total")
    private UnsignedLong total; // Total supply in lamports.

    @Json(name = "circulating")
    private UnsignedLong circulating; // Circulating supply in lamports.

    @Json(name = "nonCirculating")
    private UnsignedLong nonCirculating; // Non-circulating supply in lamports.

    @Json(name = "nonCirculatingAccounts")
    private List<PublicKey> nonCirculatingAccounts; // List of non-circulating account addresses as base-58 encoded strings.
}