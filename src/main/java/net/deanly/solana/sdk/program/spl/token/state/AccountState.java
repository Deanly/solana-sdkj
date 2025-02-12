package net.deanly.solana.sdk.program.spl.token.state;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.Struct;
import net.deanly.solana.sdk.layout.field.AccountStateEnumField;
import net.deanly.solana.sdk.layout.field.PublicKeyBorshOptionField;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.layout.field.UInt64LECOptionField;
import net.deanly.solana.sdk.program.spl.token.type.AccountStateEnum;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.guava.UnsignedLong;

@Getter
@ToString
@NoArgsConstructor
// https://docs.rs/spl-token/latest/spl_token/state/struct.Account.html
public class AccountState extends Struct {
    public static final int BYTES_LENGTH = 165;

    @StructField(order = 1, type = PublicKeyField.class)
    PublicKey mint; // 토큰 발행자 (32 bytes)

    @StructField(order = 2, type = PublicKeyField.class)
    PublicKey owner; // 계정 소유자 (32 bytes)

    @StructField(order = 3, type = UInt64LEField.class)
    UnsignedLong amount; // 보유한 토큰 수량 (u64 - 8 bytes)

    @StructField(order = 4, type = PublicKeyBorshOptionField.class)
    PublicKey delegate; // (Optional) 위임된 토큰 계정 (32 bytes, COption)

    @StructField(order = 5, type = AccountStateEnumField.class)
    AccountStateEnum state; // 계정 상태 - Enum (1 byte, AccountState)

    @StructField(order = 6, type = UInt64LECOptionField.class)
    UnsignedLong isNative; // (Optional) 네이티브 계정 여부 (COption)

    @StructField(order = 7, type = UInt64LEField.class)
    UnsignedLong delegatedAmount; // 위임된 토큰 수량 (8 bytes)

    @StructField(order = 8, type = PublicKeyBorshOptionField.class)
    PublicKey closeAuthority; // (Optional) 계정을 닫을 수 있는 사용자 권한 (32 bytes, COption)

    public static AccountState unpack(byte[] bytes) {
        return StructLayout.decode(bytes, AccountState.class);
    }


}
