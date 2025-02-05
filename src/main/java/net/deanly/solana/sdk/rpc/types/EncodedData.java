package net.deanly.solana.sdk.rpc.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EncodedData {
    private final String value;
    private final Encoding encoding;
}
