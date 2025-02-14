package net.deanly.solana.sdk.program.spl.token.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.Struct;
import net.deanly.solana.sdk.program.spl.token.SplTokenProgram;
import net.deanly.solana.sdk.rpc.client.config.Network;

abstract class SplTokenInstructionBase extends Struct {

    public PublicKey getProgramId(Network network) {
        return SplTokenProgram.PROGRAM_ID;
    }
    public PublicKey getProgramId() {
        return SplTokenProgram.PROGRAM_ID;
    }
}