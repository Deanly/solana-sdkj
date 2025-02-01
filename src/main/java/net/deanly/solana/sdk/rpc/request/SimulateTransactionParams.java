package net.deanly.solana.sdk.rpc.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.deanly.solana.sdk.rpc.config.SimulateTransactionConfig;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimulateTransactionParams {
    private String transaction;
    private SimulateTransactionConfig config;

    public List<Object> toParams() {
        return List.of(transaction, config);
    }
}
