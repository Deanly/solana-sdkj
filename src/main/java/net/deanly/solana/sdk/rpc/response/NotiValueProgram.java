package net.deanly.solana.sdk.rpc.response;

import net.deanly.solana.sdk.crypto.PublicKey;

public class NotiValueProgram extends ResValueProgram {
    NotiValueProgram(ResValueAccountInfo account, PublicKey pubkey) {
        super(account, pubkey);
    }
}
