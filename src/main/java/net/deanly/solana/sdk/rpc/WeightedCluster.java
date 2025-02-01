package net.deanly.solana.sdk.rpc;

import lombok.*;
import net.deanly.solana.sdk.rpc.types.WeightedEndpoint;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeightedCluster {

    List<WeightedEndpoint> endpoints;

}
