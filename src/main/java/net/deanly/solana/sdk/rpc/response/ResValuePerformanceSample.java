package net.deanly.solana.sdk.rpc.response;

import net.deanly.structlayout.type.guava.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValuePerformanceSample {

    @Json(name = "slot")
    private UnsignedLong slot; // Slot in which the sample was taken.

    @Json(name = "numTransactions")
    private UnsignedLong numTransactions; // Number of transactions processed during the sample period.

    @Json(name = "numSlots")
    private UnsignedLong numSlots; // Number of slots completed during the sample period.

    @Json(name = "samplePeriodSecs")
    private Integer samplePeriodSecs; // Number of seconds in a sample window.

    @Json(name = "numNonVoteTransactions")
    private UnsignedLong numNonVoteTransactions; // Number of non-vote transactions processed during the sample period (present starting with v1.15).
}