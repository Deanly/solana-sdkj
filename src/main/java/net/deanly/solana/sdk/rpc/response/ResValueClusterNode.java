package net.deanly.solana.sdk.rpc.response;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.Setter;
import net.deanly.solana.sdk.crypto.PublicKey;

@Getter
@Setter
public class ResValueClusterNode {
    @Json(name = "pubkey")
    private PublicKey pubkey; // Node public key, as base-58 encoded string

    @Json(name = "gossip")
    private String gossip; // Gossip network address for the node

    @Json(name = "tpu")
    private String tpu; // TPU network address for the node

    @Json(name = "rpc")
    private String rpc; // JSON RPC network address for the node, or null if not enabled

    @Json(name = "version")
    private String version; // Software version of the node, or null if unavailable

    @Json(name = "featureSet")
    private Long featureSet; // Unique identifier of the node's feature set

    @Json(name = "shredVersion")
    private Integer shredVersion; // Shred version the node is configured to use
}