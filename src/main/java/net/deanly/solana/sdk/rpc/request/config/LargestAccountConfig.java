package net.deanly.solana.sdk.rpc.request.config;

import lombok.Setter;

@Setter
public class LargestAccountConfig {

    private String filter;

    private String commitment;

}