package net.deanly.solana.sdk.program.metaplex.tokenmetadata.state;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.deanly.solana.sdk.layout.State;
import net.deanly.solana.sdk.program.metaplex.tokenmetadata.layout.UseMethodField;
import net.deanly.solana.sdk.program.metaplex.tokenmetadata.type.UseMethod;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;

/// [GitHub](https://github.com/metaplex-foundation/mpl-token-metadata/blob/main/clients/rust/src/generated/types/uses.rs)
@Data
@EqualsAndHashCode(callSuper = true)
public class UsesState extends State {

    @StructField(order = 1, type = UseMethodField.class)
    private UseMethod useMethod;

    @StructField(order = 2, type = UInt64LEField.class)
    private long remaining;

    @StructField(order = 3, type = UInt64LEField.class)
    private long total;
}
