package net.deanly.solana.sdk.program.metaplex.tokenmetadata.state;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.State;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.program.metaplex.tokenmetadata.layout.CollectionDetailsField;
import net.deanly.solana.sdk.program.metaplex.tokenmetadata.layout.TokenStandardField;
import net.deanly.solana.sdk.program.metaplex.tokenmetadata.type.CollectionDetails;
import net.deanly.solana.sdk.program.metaplex.tokenmetadata.type.TokenStandard;
import net.deanly.structlayout.annotation.OptionalEncoding;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.type.basic.Int32LEField;
import net.deanly.structlayout.type.basic.UInt16LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.structlayout.type.borsh.BorshBooleanField;
import net.deanly.structlayout.type.borsh.BorshStringField;

import java.util.List;

/// [GitHub](https://github.com/metaplex-foundation/mpl-token-metadata/blob/main/clients/rust/src/generated/accounts/metadata.rs)
@Data
@EqualsAndHashCode(callSuper = true)
public class TokenMetadataState extends State {

    @StructField(order = 1, type = UInt8Field.class)
    private int key;

    @StructField(order = 2, type = PublicKeyField.class)
    private PublicKey updateAuthority;

    @StructField(order = 3, type = PublicKeyField.class)
    private PublicKey mint;

    @StructField(order = 4, type = BorshStringField.class)
    private String name;

    @StructField(order = 5, type = BorshStringField.class)
    private String symbol;

    @StructField(order = 6, type = BorshStringField.class)
    private String uri;

    @StructField(order = 7, type = UInt16LEField.class)
    private int sellerFeeBasisPoints;

    @StructSequenceObjectField(order = 8, lengthType = Int32LEField.class, optional = OptionalEncoding.BORSH)
    private List<CreatorState> creators;

    @StructField(order = 9, type = BorshBooleanField.class)
    private Boolean primarySaleHappened;

    @StructField(order = 10, type = BorshBooleanField.class)
    private Boolean isMutable;

    @StructField(order = 11, type = UInt8Field.class, optional = OptionalEncoding.BORSH)
    private Integer editionNonce;

    @StructField(order = 12, type = TokenStandardField.class, optional = OptionalEncoding.BORSH)
    private TokenStandard tokenStandard;

    @StructObjectField(order = 13, optional = OptionalEncoding.BORSH)
    private CollectionState collection;

    @StructObjectField(order = 14, optional = OptionalEncoding.BORSH)
    private UsesState uses;

    @StructField(order = 15, type = CollectionDetailsField.class, optional = OptionalEncoding.BORSH)
    private CollectionDetails collectionDetails;

    @StructObjectField(order = 16, optional = OptionalEncoding.BORSH)
    private ProgrammableConfigState programmableConfig;
}
