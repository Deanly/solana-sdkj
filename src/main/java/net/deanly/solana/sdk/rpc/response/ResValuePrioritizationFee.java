package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.structlayout.type.guava.UnsignedLong;

@Getter
@ToString
public class ResValuePrioritizationFee {

    @Json(name = "slot")
    private UnsignedLong slot; // Slot in which the fee was observed.

    @Json(name = "prioritizationFee")
    private UnsignedLong prioritizationFee; // The per-compute-unit fee paid by at least one successfully landed transaction, specified in increments of micro-lamports (0.000001 lamports).
}