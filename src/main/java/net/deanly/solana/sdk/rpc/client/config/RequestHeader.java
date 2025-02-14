package net.deanly.solana.sdk.rpc.client.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@lombok.Builder
@RequiredArgsConstructor
public class RequestHeader {
    public final String key;
    public final String value;
}
