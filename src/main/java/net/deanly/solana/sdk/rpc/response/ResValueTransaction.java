package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.solana.sdk.types.Signature;

import java.util.List;

/// Transactions are quite different from those on other blockchains. Be sure to review
/// [Anatomy of a Transaction](https://solana.com/ko/docs/core/transactions) to learn about
/// transactions on Solana.
@Getter
@ToString
public class ResValueTransaction {
    /// Defines the content of the transaction
    @Json(name = "message")
    private Message message;

    /// List of address table lookups used by a transaction to dynamically load addresses
    /// from on-chain address lookup tables. Undefined if maxSupportedTransactionVersion is not set.
    @Json(name = "addressTableLookups")
    private List<AddressTableLookup> addressTableLookups;

    /// A list of base-58 encoded signatures applied to the transaction. The list is always of length
    /// `message.header.numRequiredSignatures` and not empty. The signature at index i corresponds to t
    /// he public key at index i in `message.accountKeys`. The first one is used as the transaction id.
    @Json(name = "signatures")
    private List<Signature> signatures;


    @Getter
    @ToString
    public static class Message {
        /// List of base-58 encoded public keys used by the transaction, including by the instructions
        /// and for signatures. The first `message.header.numRequiredSignatures` public keys must
        /// sign the transaction.
        @Json(name = "accountKeys")
        private List<PublicKey> accountKeys;

        /// Details the account types and signatures required by the transaction.
        @Json(name = "header")
        private Header header;

        /// A base-58 encoded hash of a recent block in the ledger used to prevent transaction
        /// duplication and to give transactions lifetimes.
        @Json(name = "recentBlockhash")
        private Blockhash recentBlockhash;

        /// List of program instructions that will be executed in sequence and committed in one
        /// atomic transaction if all succeed.
        @Json(name = "instructions")
        private List<ResValueInstruction> instructions;

        @Getter
        @ToString
        public static class Header {
            /// The total number of signatures required to make the transaction valid.
            /// The signatures must match the first `numRequiredSignatures` of `message.accountKeys`.
            @Json(name = "numReadonlySignedAccounts")
            private Integer numReadonlySignedAccounts;

            /// The last `numReadonlySignedAccounts` of the signed keys are read-only accounts.
            /// Programs may process multiple transactions that load read-only accounts within
            /// a single PoH entry, but are not permitted to credit or debit lamports or modify
            /// account data. Transactions targeting the same read-write account are evaluated sequentially.
            @Json(name = "numReadonlyUnsignedAccounts")
            private Integer numReadonlyUnsignedAccounts;

            /// The last numReadonlyUnsignedAccounts of the unsigned keys are read-only accounts.
            @Json(name = "numRequiredSignatures")
            private Integer numRequiredSignatures;
        }
    }

    @Getter
    @ToString
    public static class AddressTableLookup {
        /// base-58 encoded public key for an address lookup table account.
        @Json(name = "accountKey")
        private PublicKey accountKey;

        /// List of indices used to load addresses of writable accounts from a lookup table.
        @Json(name = "writableIndexes")
        private List<Integer> writableIndexes;

        /// List of indices used to load addresses of readonly accounts from a lookup table.
        @Json(name = "readonlyIndexes")
        private List<Integer> readonlyIndexes;
    }
}
