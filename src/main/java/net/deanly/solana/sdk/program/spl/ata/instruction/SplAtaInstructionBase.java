package net.deanly.solana.sdk.program.spl.ata.instruction;

import lombok.Getter;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.exception.ProgramException;
import net.deanly.solana.sdk.program.spl.ata.AssociatedTokenAccountProgram;
import net.deanly.solana.sdk.program.spl.token.SplTokenProgram;
import net.deanly.solana.sdk.rpc.client.Network;

import java.util.List;

abstract class SplAtaInstructionBase {

    public PublicKey getProgramId() {
        return AssociatedTokenAccountProgram.PROGRAM_ID;
    }

    public PublicKey getProgramId(Network network) {
        return AssociatedTokenAccountProgram.PROGRAM_ID;
    }

    protected static PublicKey findAssociatedTokenAddress(PublicKey walletAddress, PublicKey mint) {
        try {
            return PublicKey.findProgramAddress(
                    List.of(
                            walletAddress.toByteArray(),
                            SplTokenProgram.PROGRAM_ID.toByteArray(),
                            mint.toByteArray()
                    ),
                    AssociatedTokenAccountProgram.PROGRAM_ID
            ).getAddress();
        } catch (Exception e) {
            throw new ProgramException("Failed to find associated token address", e);
        }
    }
}
