package net.deanly.solana.sdk.program.spl.alt.instruction;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.spl.alt.AddressLookupTableProgram;
import net.deanly.solana.sdk.rpc.client.Network;

abstract class AtlInstructionBase {
    public PublicKey getProgramId(Network network) {
        return AddressLookupTableProgram.PROGRAM_ID;
    }

    public PublicKey getProgramId() {
        return AddressLookupTableProgram.PROGRAM_ID;
    }
}