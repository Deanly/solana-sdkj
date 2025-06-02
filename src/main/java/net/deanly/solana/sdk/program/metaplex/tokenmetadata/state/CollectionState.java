package net.deanly.solana.sdk.program.metaplex.tokenmetadata.state;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.State;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.borsh.BorshBooleanField;

/// [GitHub](https://github.com/orgs/metaplex-foundation/discussions/444)
@Data
@EqualsAndHashCode(callSuper = true)
public class CollectionState extends State {

    @StructField(order = 1, type = BorshBooleanField.class)
    private Boolean verified;

    @StructField(order = 2, type = PublicKeyField.class)
    private PublicKey key;
}