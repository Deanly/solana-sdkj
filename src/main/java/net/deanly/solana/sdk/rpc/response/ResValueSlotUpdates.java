package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueSlotUpdates {

    @Json(name = "err")
    private String err;

    @Json(name = "parent")
    private Long parent;

    @Json(name = "slot")
    private Long slot;

    @Json(name = "stats")
    private SlotStats stats;

    @Json(name = "type")
    private String type;

    @Json(name = "timestamp")
    private Long timestamp;

    @Getter
    @ToString
    public static class SlotStats {

        @Json(name = "maxTransactionsPerEntry")
        private Long maxTransactionsPerEntry;

        @Json(name = "numFailedTransactions")
        private Long numFailedTransactions;

        @Json(name = "numSuccessfulTransactions")
        private Long numSuccessfulTransactions;

        @Json(name = "numTransactionEntries")
        private Long numTransactionEntries;
    }
}