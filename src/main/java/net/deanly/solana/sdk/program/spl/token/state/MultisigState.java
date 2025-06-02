package net.deanly.solana.sdk.program.spl.token.state;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.State;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.type.advanced.NoneField;
import net.deanly.structlayout.type.basic.UInt8Field;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
// https://docs.rs/spl-token/latest/spl_token/state/struct.Multisig.html
public class MultisigState extends State {
    public static final int BYTES_LENGTH = 355;

    @StructField(order = 1, type = UInt8Field.class)
    int isInitialized; // 초기화 여부 (1 byte, bool)

    @StructField(order = 2, type = UInt8Field.class)
    int m; // 필요한 서명 수 (1 byte)

    @StructField(order = 3, type = UInt8Field.class)
    int n; // 등록된 서명자 수 (1 byte)

    // TODO: Add a fixed-length sequencing field annotation
    @StructSequenceField(order = 4, elementType = PublicKeyField.class, lengthType = NoneField.class)
    List<PublicKey> signers; // 서명자 배열 (32 bytes * 최대 11)

    public static MultisigState unpack(byte[] bytes) {
        return StructLayout.decode(bytes, MultisigState.class);
    }
}