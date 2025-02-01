package net.deanly.solana.sdk.rpc.config;

import lombok.Setter;

@Setter
public class RpcEpochConfig {

    private Long epoch;

    private String commitment;

}