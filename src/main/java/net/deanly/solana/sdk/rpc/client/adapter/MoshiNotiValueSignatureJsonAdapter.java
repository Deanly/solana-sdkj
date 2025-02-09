package net.deanly.solana.sdk.rpc.client.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.internal.Util;
import net.deanly.solana.sdk.rpc.response.NotiValueSignature;
import net.deanly.solana.sdk.types.TransactionError;
import net.deanly.solana.sdk.types.SignatureStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MoshiNotiValueSignatureJsonAdapter extends JsonAdapter<NotiValueSignature> {
    private final JsonAdapter<TransactionError> transactionErrorAdapter;
    private final JsonAdapter<SignatureStatus> receivedSignatureAdapter;

    public MoshiNotiValueSignatureJsonAdapter(Moshi moshi) {
        this.transactionErrorAdapter = moshi.nextAdapter(
                FACTORY,
                TransactionError.class,
                Util.NO_ANNOTATIONS
        );
        this.receivedSignatureAdapter = moshi.nextAdapter(
                FACTORY,
                SignatureStatus.class,
                Util.NO_ANNOTATIONS
        );
    }

    @Nullable
    @Override
    public NotiValueSignature fromJson(JsonReader reader) throws IOException {
        if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
            TransactionError error = null;

            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "error":
                        error = transactionErrorAdapter.fromJson(reader);
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();

            return NotiValueSignature.builder()
                    .error(error)
                    .status(SignatureStatus.ERROR)
                    .build();
        } else if (reader.peek() == JsonReader.Token.STRING) {
            SignatureStatus received = receivedSignatureAdapter.fromJson(reader);
            return NotiValueSignature.builder()
                    .status(received)
                    .build();
        } else {
            throw new IOException("Invalid JSON format for NotiValueSignature");
        }
    }

    @Override
    public void toJson(JsonWriter writer, @Nullable NotiValueSignature value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }

        writer.beginObject();
        if (value.getError() != null) {
            writer.name("error");
            transactionErrorAdapter.toJson(writer, value.getError());
        }
        if (value.getStatus() != null) {
            writer.name("received");
            receivedSignatureAdapter.toJson(writer, value.getStatus());
        }
        writer.endObject();
    }

    public static final JsonAdapter.Factory FACTORY = (type, annotations, moshi) -> {
        if (type == NotiValueSignature.class) {
            return new MoshiNotiValueSignatureJsonAdapter(moshi);
        }
        return null;
    };
}