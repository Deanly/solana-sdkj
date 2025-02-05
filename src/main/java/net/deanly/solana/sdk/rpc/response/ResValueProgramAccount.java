package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ResValueProgramAccount {

    @Json(name = "pubkey")
    private String pubkey; // The account Pubkey as a base-58 encoded string.

    @Json(name = "account")
    private Account account; // The account details.

    @Getter
    @ToString
    public static class Account {
        @Json(name = "lamports")
        private long lamports; // Number of lamports assigned to this account.

        @Json(name = "owner")
        private String owner; // Base-58 encoded Pubkey of the program this account has been assigned to.

        @Json(name = "data")
        private Object data; // Data associated with the account, either as encoded binary data or JSON format.

        @Json(name = "executable")
        private boolean executable; // Indicates if the account contains a program (and is strictly read-only).

        @Json(name = "rentEpoch")
        private long rentEpoch; // The epoch at which this account will next owe rent.
    }
}