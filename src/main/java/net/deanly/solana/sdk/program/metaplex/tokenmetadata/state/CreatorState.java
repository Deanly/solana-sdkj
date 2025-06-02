package net.deanly.solana.sdk.program.metaplex.tokenmetadata.state;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.State;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.structlayout.type.borsh.BorshBooleanField;

/// [GitHub](https://github.com/metaplex-foundation/mpl-token-metadata/blob/main/clients/rust/src/generated/types/creator.rs)
@Getter
@NoArgsConstructor
@ToString
public class CreatorState extends State {

    @StructField(order = 1, type = PublicKeyField.class)
    PublicKey address;

    @StructField(order = 2, type = BorshBooleanField.class)
    Boolean verified;

    @StructField(order = 3, type = UInt8Field.class)
    Short share;

}