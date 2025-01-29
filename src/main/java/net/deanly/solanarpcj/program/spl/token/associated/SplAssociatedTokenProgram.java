package net.deanly.solanarpcj.program.spl.token.associated;

import lombok.Getter;
import lombok.NonNull;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.layout.Struct;
import net.deanly.solanarpcj.program.exception.ProgramException;
import net.deanly.solanarpcj.program.spl.token.associated.instruction.SplAssociatedTokenInstruction0Create;
import net.deanly.solanarpcj.program.spl.token.associated.instruction.SplAssociatedTokenInstruction1CreateIdempotent;
import net.deanly.solanarpcj.program.spl.token.associated.instruction.SplAssociatedTokenInstruction2RecoverNested;
import net.deanly.solanarpcj.program.spl.token.basic.SplTokenProgram;

import java.util.List;

public class SplAssociatedTokenProgram extends Struct {

    public static final PublicKey PROGRAM_ID = new PublicKey("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL");;

    @Getter
    public static class Base extends Struct {
        private final PublicKey programId = PROGRAM_ID;

        protected static PublicKey findAssociatedTokenAddress(PublicKey walletAddress, PublicKey mint) {
            try {
                return PublicKey.findProgramAddress(
                        List.of(
                                walletAddress.toByteArray(),
                                SplTokenProgram.PROGRAM_ID.toByteArray(),
                                mint.toByteArray()
                        ),
                        PROGRAM_ID
                ).getAddress();
            } catch (Exception e) {
                throw new ProgramException("Failed to find associated token address", e);
            }
        }
    }

    /**
     * Static method for creating a standard associated token account instruction.
     */
    public static SplAssociatedTokenInstruction0Create create(
            @NonNull PublicKey payer,
            @NonNull PublicKey owner,
            @NonNull PublicKey mint
    ) {
        return SplAssociatedTokenInstruction0Create.create(
                payer,
                owner,
                mint
        );
    }

    /**
     * Static method for creating an idempotent associated token account instruction.
     */
    public static SplAssociatedTokenInstruction1CreateIdempotent createIdempotent(
            @NonNull PublicKey payer,
            @NonNull PublicKey owner,
            @NonNull PublicKey mint
    ) {
        return SplAssociatedTokenInstruction1CreateIdempotent.create(
                payer,
                owner,
                mint
        );
    }

    /**
     * Static method for recovering a nested associated token account.
     */
    public static SplAssociatedTokenInstruction2RecoverNested recoverNested(
            @NonNull PublicKey nestedAssociatedAccountAddress,
            @NonNull PublicKey nestedTokenMintAddress,
            @NonNull PublicKey destinationAssociatedAccountAddress,
            @NonNull PublicKey ownerAssociatedAccountAddress,
            @NonNull PublicKey ownerTokenMintAddress,
            @NonNull PublicKey walletAddress
    ) {
        return SplAssociatedTokenInstruction2RecoverNested.create(
                nestedAssociatedAccountAddress,
                nestedTokenMintAddress,
                destinationAssociatedAccountAddress,
                ownerAssociatedAccountAddress,
                ownerTokenMintAddress,
                walletAddress
        );
    }
}
