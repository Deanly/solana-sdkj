package net.deanly.solana.sdk.rpc.request.config;

import lombok.Setter;

@Setter
public class VoteAccountConfig {

    private String votePubkey;

    private String commitment;

}
