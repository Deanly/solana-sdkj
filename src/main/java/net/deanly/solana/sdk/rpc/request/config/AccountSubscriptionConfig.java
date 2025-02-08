package net.deanly.solana.sdk.rpc.request.config;

import lombok.Builder;
import lombok.Getter;
import net.deanly.solana.sdk.types.Commitment;
import net.deanly.solana.sdk.types.Encoding;

@Getter
@Builder
public class AccountSubscriptionConfig {
    private final Commitment commitment;
    private final Encoding encoding;
}