package net.deanly.solana.sdk.rpc.config;

import lombok.Setter;

@Setter
public class VoteAccountConfig {

    private String votePubkey;

    private String commitment;

}
