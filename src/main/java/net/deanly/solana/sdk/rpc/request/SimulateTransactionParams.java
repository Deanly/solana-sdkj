package net.deanly.solana.sdk.rpc.request;

import lombok.*;
import net.deanly.solana.sdk.rpc.request.config.SimulateTransactionConfig;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimulateTransactionParams {

    @NonNull
    private String transaction;

    @NonNull
    private SimulateTransactionConfig config;

    public List<Object> toParams() {
        return List.of(transaction, config);
    }

}
