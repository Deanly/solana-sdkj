package net.deanly.solana.sdk.rpc.request.filter;

import lombok.*;
import net.deanly.solana.sdk.crypto.PublicKey;


/// filter criteria for the logs to receive results by account type; currently supported:
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@lombok.Builder
public class BlockFilter {
    /// If null, include all transactions in block.
    /// Otherwise, set value to return only transactions that mention the provided
    /// public key (as base-58 encoded string). If no mentions are found in a given block,
    /// then no notification will be sent.
    private PublicKey mentionsAccountOrProgram;
}
