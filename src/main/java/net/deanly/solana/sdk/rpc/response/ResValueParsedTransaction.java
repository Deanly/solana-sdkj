package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.solana.sdk.types.Signature;
import net.deanly.solana.sdk.types.StateData;

import java.util.List;

@Getter
@ToString
public class ResValueParsedTransaction {

    /// Defines the content of the transaction
    @Json(name = "message")
    private ParsedMessage message;

    /// A list of base-58 encoded signatures applied to the transaction.
    @Json(name = "signatures")
    private List<Signature> signatures;

    @Getter
    @ToString
    public static class ParsedMessage {
        /// List of account information used by the transaction.
        @Json(name = "accountKeys")
        private List<ParsedAccountKey> accountKeys;

        /// List of parsed program instructions.
        @Json(name = "instructions")
        private List<ParsedInstruction> instructions;

        /// A base-58 encoded hash of a recent block in the ledger used to prevent transaction duplication and to give transactions lifetimes.
        @Json(name = "recentBlockhash")
        private Blockhash recentBlockhash;
    }

    @Getter
    @ToString
    public static class ParsedAccountKey {
        /// The base-58 encoded public key of the account.
        @Json(name = "pubkey")
        private PublicKey pubkey;

        /// Indicates if the account is a required transaction signer.
        @Json(name = "signer")
        private boolean signer;

        /// Indicates if the account is writable.
        @Json(name = "writable")
        private boolean writable;

        /// Source of the account (transaction or lookup table).
        @Json(name = "source")
        private String source;
    }

    @Getter
    @ToString
    public static class ParsedInstruction {
        /// Program-specific parsed data.
        @Json(name = "parsed")
        private StateData parsed;

        /// The name of the program being called.
        @Json(name = "program")
        private String program;

        /// The base-58 encoded public key of the program.
        @Json(name = "programId")
        private String programId;

        /// The stack height of the instruction.
        @Json(name = "stackHeight")
        private Integer stackHeight;
    }
}