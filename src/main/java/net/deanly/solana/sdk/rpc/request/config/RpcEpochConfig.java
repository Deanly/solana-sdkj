package net.deanly.solana.sdk.rpc.request.config;

import lombok.Setter;

@Setter
public class RpcEpochConfig {

    private Long epoch;

    private String commitment;

}