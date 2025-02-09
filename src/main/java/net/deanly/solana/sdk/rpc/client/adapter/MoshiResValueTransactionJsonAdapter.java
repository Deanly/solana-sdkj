package net.deanly.solana.sdk.rpc.client.adapter;
import com.squareup.moshi.*;
import com.squareup.moshi.internal.Util;
import net.deanly.solana.sdk.rpc.response.ResValueTransaction;
import net.deanly.solana.sdk.transaction.Transaction;
import net.deanly.solana.sdk.types.codec.Base58;

import java.io.IOException;
import java.util.Base64;

public class MoshiResValueTransactionJsonAdapter extends JsonAdapter<ResValueTransaction> {
    private final JsonAdapter<ResValueTransaction> moshiAdapter;

    public MoshiResValueTransactionJsonAdapter(Moshi moshi) {
        this.moshiAdapter = moshi.nextAdapter(
                FACTORY,
                ResValueTransaction.class,
                Util.NO_ANNOTATIONS
        );
    }

    @Override
    public ResValueTransaction fromJson(JsonReader reader) throws IOException {
        JsonReader.Token token = reader.peek();

        if (token == JsonReader.Token.BEGIN_OBJECT) {
            // JSON object - directly parse into ResValueTransaction
            return parseResValueTransaction(reader);
        } else if (token == JsonReader.Token.BEGIN_ARRAY) {
            // JSON array - [data, encoding]
            return parseEncodedTransaction(reader);
        } else {
            throw new JsonDataException("Unexpected JSON format for ResValueTransaction: " + token);
        }
    }

    private ResValueTransaction parseResValueTransaction(JsonReader reader) throws IOException {
        return this.moshiAdapter.fromJson(reader);
    }

    private ResValueTransaction parseEncodedTransaction(JsonReader reader) throws IOException {
        String data = null;
        String encoding = null;

        reader.beginArray();
        if (reader.hasNext()) {
            data = reader.nextString();
        }
        if (reader.hasNext()) {
            encoding = reader.nextString();
        }
        reader.endArray();

        if (data == null || encoding == null) {
            throw new JsonDataException("Both data and encoding are required for encoded transaction");
        }

        // Decode the data based on the encoding type
        byte[] decodedData = decodeData(data, encoding);

        // Deserialize the transaction
        Transaction transaction = Transaction.deserialize(decodedData);

        // Convert the Transaction to ResValueTransaction
        return transaction.toResValueTransaction();
    }

    private byte[] decodeData(String data, String encoding) {
        switch (encoding.toLowerCase()) {
            case "base58":
                return Base58.decode(data);
            case "base64":
                return Base64.getDecoder().decode(data);
            default:
                throw new IllegalArgumentException("Unsupported encoding type: " + encoding);
        }
    }

    @Override
    public void toJson(JsonWriter writer, ResValueTransaction value) throws IOException {
        // This method is intentionally left empty since serialization is not needed
        throw new UnsupportedOperationException("Serialization is not supported for ResValueTransaction");
    }

    public static final JsonAdapter.Factory FACTORY = (type, annotations, moshi) -> {
        if (Types.equals(type, ResValueTransaction.class)) {
            return new MoshiResValueTransactionJsonAdapter(moshi);
        }
        return null;
    };
}