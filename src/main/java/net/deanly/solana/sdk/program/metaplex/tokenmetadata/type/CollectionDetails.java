package net.deanly.solana.sdk.program.metaplex.tokenmetadata.type;

import lombok.Data;
import lombok.EqualsAndHashCode;

/// [GitHub](https://github.com/metaplex-foundation/mpl-token-metadata/blob/main/clients/rust/src/generated/types/collection_details.rs)
public abstract class CollectionDetails {

    public static final int V1 = 0;
    public static final int V2 = 1;

    public int variantTag() {
        if (this instanceof V1) return V1;
        if (this instanceof V2) return V2;
        throw new IllegalStateException("Unknown CollectionDetails variant");
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class V1 extends CollectionDetails {
        private final long size;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class V2 extends CollectionDetails {
        private final byte[] padding; // length = 8
        public V2(byte[] padding) {
            if (padding == null || padding.length != 8) {
                throw new IllegalArgumentException("V2 padding must be 8 bytes.");
            }
            this.padding = padding;
        }
    }
}
