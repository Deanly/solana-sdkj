package net.deanly.solana.sdk.rpc.response;

import com.google.common.primitives.UnsignedLong;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.types.SlotUpdateType;

@Getter
@ToString
public class ResValueSlotUpdates {

    @Json(name = "err")
    private String err;

    @Json(name = "parent")
    private UnsignedLong parent;

    @Json(name = "slot")
    private UnsignedLong slot;

    @Json(name = "stats")
    private SlotStats stats;

    @Json(name = "type")
    private SlotUpdateType type;

    @Json(name = "timestamp")
    private Long timestamp;

    @Getter
    @ToString
    public static class SlotStats {

        @Json(name = "maxTransactionsPerEntry")
        private UnsignedLong maxTransactionsPerEntry;

        @Json(name = "numFailedTransactions")
        private UnsignedLong numFailedTransactions;

        @Json(name = "numSuccessfulTransactions")
        private UnsignedLong numSuccessfulTransactions;

        @Json(name = "numTransactionEntries")
        private UnsignedLong numTransactionEntries;
    }
}