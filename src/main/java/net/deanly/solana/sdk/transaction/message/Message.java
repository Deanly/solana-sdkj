package net.deanly.solana.sdk.transaction.message;

import lombok.*;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.layout.Struct;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.layout.field.Base58Bytes32Field;
import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.solana.sdk.layout.field.PublicKeyField;
import net.deanly.solana.sdk.layout.field.ShortVecField;
import net.deanly.solana.sdk.transaction.message.compiler.MessageCompiler;
import net.deanly.solana.sdk.transaction.message.meta.MessageCompiledInstruction;
import net.deanly.solana.sdk.transaction.message.meta.MessageHeader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Message extends Struct implements VersionedMessage {

    @StructObjectField(order = 1)
    protected MessageHeader header;

    @StructSequenceField(order = 2, elementType = PublicKeyField.class, lengthType = ShortVecField.class)
    protected List<PublicKey> staticAccountKeys;

    @Setter
    @StructField(order = 3, type = Base58Bytes32Field.class)
    protected String recentBlockhash;

    @StructSequenceObjectField(order = 4, lengthType = ShortVecField.class)
    protected List<MessageCompiledInstruction> instructions;

    public Message(MessageHeader messageHeader, List<PublicKey> staticAccountKeys, Blockhash recentBlockhash, List<MessageCompiledInstruction> instructions) {
        this.header = messageHeader;
        this.staticAccountKeys = staticAccountKeys;
        this.recentBlockhash = recentBlockhash.getValue();
        this.instructions = instructions;
    }

    @Override
    public Version getVersion() {
        return Version.LEGACY;
    }

    @Override
    public List<PublicKey> getSigners() {
        List<PublicKey> signers = new ArrayList<>();
        for (int i = 0; i < header.getNumRequiredSignatures(); i++) {
            signers.add(staticAccountKeys.get(i));
        }
        return signers;
    }

    /**
     * Compile the message using the payer key, instructions, and recent blockhash.
     */
    public static Message compile(PublicKey payerKey, List<TransactionInstruction> instructions, Blockhash recentBlockhash) {
        return MessageCompiler.compileLegacy(payerKey, instructions, recentBlockhash);
    }

    /**
     * Serialize the message to a byte array.
     */
    @Override
    public byte[] serialize() {
        if (recentBlockhash == null) {
            throw new IllegalArgumentException("Recent blockhash is required");
        }
        return StructLayout.encode(this);
    }

    public static Message deserialize(ByteBuffer buffer) {
        return StructLayout.decode(buffer.array(), Message.class);
    }

    public static Message deserialize(byte[] serializedMessage) {
        ByteBuffer buffer = ByteBuffer.wrap(serializedMessage).order(ByteOrder.LITTLE_ENDIAN);
        return deserialize(buffer);
    }

    public boolean isAccountSigner(int accountIndex) {
        if (accountIndex < 0 || accountIndex >= staticAccountKeys.size()) {
            throw new IndexOutOfBoundsException("Account index is out of range");
        }
        return accountIndex < header.getNumRequiredSignatures();
    }

    public boolean isAccountWritable(int accountIndex) {
        int numSignedAccounts = header.getNumRequiredSignatures();
        if (accountIndex >= header.getNumRequiredSignatures()) {
            int unsignedAccountIndex = accountIndex - numSignedAccounts;
            int numUnsignedAccounts = staticAccountKeys.size() - numSignedAccounts;
            int numWritableUnsignedAccounts = numUnsignedAccounts - header.getNumReadonlyUnsignedAccounts();
            return unsignedAccountIndex < numWritableUnsignedAccounts;
        } else {
            int numWritableSignedAccounts = numSignedAccounts - header.getNumReadonlySignedAccounts();
            return accountIndex < numWritableSignedAccounts;
        }
    }
}
