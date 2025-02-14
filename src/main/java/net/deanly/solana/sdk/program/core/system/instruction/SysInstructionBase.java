package net.deanly.solana.sdk.program.core.system.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.solana.sdk.rpc.client.config.Network;

abstract class SysInstructionBase {
    public PublicKey getProgramId(Network network) {
        return SystemProgram.PROGRAM_ID;
    }

    public PublicKey getProgramId() {
        return SystemProgram.PROGRAM_ID;
    }
}
