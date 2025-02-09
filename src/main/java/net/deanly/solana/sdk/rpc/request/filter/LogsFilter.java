package net.deanly.solana.sdk.rpc.request.filter;

import lombok.*;
import net.deanly.solana.sdk.crypto.PublicKey;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@lombok.Builder
public class LogsFilter {

    /// - `ALL` : subscribe to all transactions except for simple vote transactions
    /// - `ALL_WITH_VOTES` : subscribe to all transactions, including simple vote transactions
    /// - `MENTIONS` : using `mentions` field
    @lombok.Builder.Default
    private Type type = Type.MENTIONS;

    /// Array containing a single Pubkey (as base-58 encoded string); if present, subscribe to only transactions mentioning this address
    /// The mentions field currently only supports one Pubkey string per method call. Listing additional addresses will result in an error.
    private List<PublicKey> mentions;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        ALL("all"), ALL_WITH_VOTES("allWithVotes"), MENTIONS("mentions");
        private final String value;
    }
}
