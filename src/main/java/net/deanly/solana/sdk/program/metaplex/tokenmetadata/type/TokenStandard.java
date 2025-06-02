package net.deanly.solana.sdk.program.metaplex.tokenmetadata.type;

/// [GitHub](https://github.com/metaplex-foundation/mpl-token-metadata/blob/main/clients/rust/src/generated/types/token_standard.rs)
public enum TokenStandard {
    NON_FUNGIBLE(0),
    FUNGIBLE_ASSET(1),
    FUNGIBLE(2),
    NON_FUNGIBLE_EDITION(3),
    PROGRAMMABLE_NON_FUNGIBLE(4),
    PROGRAMMABLE_NON_FUNGIBLE_EDITION(5);

    private final int value;

    TokenStandard(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TokenStandard fromValue(int value) {
        for (TokenStandard ts : values()) {
            if (ts.value == value) {
                return ts;
            }
        }
        throw new IllegalArgumentException("Unknown TokenStandard value: " + value);
    }
}