package net.deanly.solana.sdk.program.spl.token.state;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.State;
import net.deanly.solana.sdk.layout.field.PublicKeyBorshOptionField;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.structlayout.type.guava.UnsignedLong;

@Getter
@ToString
@NoArgsConstructor
// https://docs.rs/spl-token/latest/spl_token/state/struct.Mint.html
public class MintState extends State {
    public static final int BYTES_LENGTH = 82;

    @StructField(order = 1, type = PublicKeyBorshOptionField.class)
    PublicKey mintAuthority; // (Optional) Mint 권한 (32 bytes)

    @StructField(order = 2, type = UInt64LEField.class)
    UnsignedLong supply; // 현재 전체 발행량 (8 bytes)

    @StructField(order = 3, type = UInt8Field.class)
    int decimals; // 소수점 자릿수 (1 byte)

    @StructField(order = 4, type = UInt8Field.class)
    int isInitialized; // 초기화 여부 (1 byte, bool)

    @StructField(order = 5, type = PublicKeyBorshOptionField.class)
    PublicKey freezeAuthority; // (Optional) 동결 권한 (32 bytes)

    public static MintState unpack(byte[] bytes) {
        return StructLayout.decode(bytes, MintState.class);
    }
}
