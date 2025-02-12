package net.deanly.solana.sdk.program.raydium.cpmm.instruction;

import lombok.Getter;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.raydium.cpmm.RaydiumCpmmProgram;
import net.deanly.solana.sdk.rpc.client.Network;

@Getter
abstract class CpmmInstructionBase {

    public PublicKey getProgramId(Network network) {
        switch (network) {
            case MAINNET:
                return RaydiumCpmmProgram.PROGRAM_ID_MAINNET;
            case DEVNET:
                return RaydiumCpmmProgram.PROGRAM_ID_DEVNET;
            default:
                throw new IllegalArgumentException("Unsupported network: " + network);
        }
    }

    public PublicKey getProgramId() {
        throw new UnsupportedOperationException("Please override this method");
    }
}
