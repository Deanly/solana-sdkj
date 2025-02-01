package net.deanly.solanarpcj.program.pda;


import lombok.Getter;
import net.deanly.solanarpcj.crypto.PublicKey;

@Getter
public class ProgramDerivedAddress {
    private PublicKey address;
    private int nonce;

    public ProgramDerivedAddress(PublicKey address, int nonce) {
        this.address = address;
        this.nonce = nonce;
    }
}
