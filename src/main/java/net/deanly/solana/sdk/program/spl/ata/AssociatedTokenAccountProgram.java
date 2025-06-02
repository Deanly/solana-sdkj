package net.deanly.solana.sdk.program.spl.ata;

import lombok.NonNull;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.State;
import net.deanly.solana.sdk.program.spl.ata.instruction.AssociatedTokenAccountInstruction0Create;
import net.deanly.solana.sdk.program.spl.ata.instruction.AssociatedTokenAccountInstruction1CreateIdempotent;
import net.deanly.solana.sdk.program.spl.ata.instruction.AssociatedTokenAccountInstruction2RecoverNested;

public class AssociatedTokenAccountProgram extends State {

    public static final PublicKey PROGRAM_ID = new PublicKey("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL");;

    /**
     * Static method for creating a standard associated token account instruction.
     */
    public static AssociatedTokenAccountInstruction0Create create(
            @NonNull PublicKey payer,
            @NonNull PublicKey owner,
            @NonNull PublicKey mint
    ) {
        return AssociatedTokenAccountInstruction0Create.create(
                payer,
                owner,
                mint
        );
    }

    /**
     * Static method for creating an idempotent associated token account instruction.
     */
    public static AssociatedTokenAccountInstruction1CreateIdempotent createIdempotent(
            @NonNull PublicKey payer,
            @NonNull PublicKey owner,
            @NonNull PublicKey mint
    ) {
        return AssociatedTokenAccountInstruction1CreateIdempotent.create(
                payer,
                owner,
                mint
        );
    }

    /**
     * Static method for recovering a nested associated token account.
     */
    public static AssociatedTokenAccountInstruction2RecoverNested recoverNested(
            @NonNull PublicKey nestedAssociatedAccountAddress,
            @NonNull PublicKey nestedTokenMintAddress,
            @NonNull PublicKey destinationAssociatedAccountAddress,
            @NonNull PublicKey ownerAssociatedAccountAddress,
            @NonNull PublicKey ownerTokenMintAddress,
            @NonNull PublicKey walletAddress
    ) {
        return AssociatedTokenAccountInstruction2RecoverNested.create(
                nestedAssociatedAccountAddress,
                nestedTokenMintAddress,
                destinationAssociatedAccountAddress,
                ownerAssociatedAccountAddress,
                ownerTokenMintAddress,
                walletAddress
        );
    }
}
