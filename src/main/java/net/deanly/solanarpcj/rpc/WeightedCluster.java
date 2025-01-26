package net.deanly.solanarpcj.rpc;

import lombok.*;
import net.deanly.solanarpcj.rpc.types.WeightedEndpoint;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeightedCluster {

    List<WeightedEndpoint> endpoints;

}
