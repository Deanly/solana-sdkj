package net.deanly.solana.sdk.program.raydium.clmm;

import net.deanly.solana.sdk.crypto.PublicKey;

/**
 * The {@code RaydiumClmmProgram} class provides the unique identifier for the Raydium CLMM (Concentrated Liquidity Market Making) program.
 * This identifier is represented as a {@link PublicKey}, which is used in interactions with specific Solana blockchain programs.
 */
public class RaydiumClmmProgram {

    public static final PublicKey PROGRAM_ID_MAINNET = new PublicKey("CAMMCzo5YL8w4VFF8KVHrK22GGUsp5VTaW7grrKgrWqK");
    public static final PublicKey PROGRAM_ID_DEVNET = new PublicKey("devi51mZmdwUJGU9hjN27vEz64Gps7uUefqxg27EAtH");
}
