package net.deanly.solana.sdk.rpc.response;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.State;

public record ResAccountState<T extends State>(PublicKey pubkey, T state, Exception error) {}
