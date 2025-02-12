package net.deanly.solana.sdk.rpc.response;

import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.*;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.types.StateData;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder(builderClassName = "Builder")
public class ResValueAccountInfo {

    /// number of lamports assigned to this account, as a u64
    @Json(name = "lamports")
    private UnsignedLong lamports;

    /// base-58 encoded Pubkey of the program this account has been assigned to
    @Json(name = "owner")
    private PublicKey owner;

    /// data associated with the account, either as encoded binary data or JSON format
    @Json(name = "data")
    private StateData data;

    /// boolean indicating if the account contains a program (and is strictly read-only)
    @Json(name = "executable")
    private Boolean executable;

    /// the epoch at which this account will next owe rent, as u64
    @Json(name = "rentEpoch")
    private UnsignedLong rentEpoch;

    /// the data size of the account
    @Json(name = "space")
    private UnsignedLong space;

}
