package net.deanly.solana.sdk.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlotUpdateType {
    @Json(name = "firstShredReceived")
    FIRST_SHRED_RECEIVED("firstShredReceived"),

    @Json(name = "completed")
    COMPLETED("completed"),

    @Json(name = "createdBank")
    CREATED_BANK("createdBank"),

    @Json(name = "frozen")
    FROZEN("frozen"),

    @Json(name = "dead")
    DEAD("dead"),

    @Json(name = "optimisticConfirmation")
    OPTIMISTIC_CONFIRMATION("optimisticConfirmation"),

    @Json(name = "root")
    ROOT("root");

    private final String value;
}
