package net.deanly.solana.sdk.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import com.google.common.primitives.UnsignedLong;
import java.util.List;

@Getter
@ToString
public class ResValueVoteAccounts {

    @Json(name = "current")
    private List<VoteAccount> current; // Active vote accounts.

    @Json(name = "delinquent")
    private List<VoteAccount> delinquent; // Delinquent vote accounts.

    @Getter
    @ToString
    public static class VoteAccount {

        @Json(name = "votePubkey")
        private String votePubkey; // Vote account address as base-58 encoded string.

        @Json(name = "nodePubkey")
        private String nodePubkey; // Validator identity as base-58 encoded string.

        @Json(name = "activatedStake")
        private UnsignedLong activatedStake; // Active stake assigned to this vote account.

        @Json(name = "commission")
        private int commission; // Commission charged by the vote account.

        @Json(name = "lastVote")
        private UnsignedLong lastVote; // Most recent slot voted on by this vote account.

        @Json(name = "epochVoteAccount")
        private boolean epochVoteAccount; // Indicates if the vote account is staked for this epoch.

        @Json(name = "rootSlot")
        private UnsignedLong rootSlot; // Current root slot for this vote account.
    }
}