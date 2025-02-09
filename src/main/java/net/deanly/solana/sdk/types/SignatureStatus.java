package net.deanly.solana.sdk.types;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignatureStatus {
    @Json(name = "receivedSignature")
    RECEIVED_SIGNATURE("receivedSignature"),
    PROCESSED("processed"),
    CONFIRMED("confirmed"),
    RECEIVED("received"),
    ERROR("error")
    ;
    private final String value;
}