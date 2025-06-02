package net.deanly.solana.sdk.program.metaplex.tokenmetadata.state;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.State;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.structlayout.annotation.StructField;

/// [GitHub](https://github.com/metaplex-foundation/mpl-token-metadata/blob/main/clients/rust/src/generated/types/programmable_config.rs)
@Data
@EqualsAndHashCode(callSuper = true)
public class ProgrammableConfigState extends State {

    @StructField(order = 1, type = PublicKeyField.class)
    private PublicKey ruleSet;
}
