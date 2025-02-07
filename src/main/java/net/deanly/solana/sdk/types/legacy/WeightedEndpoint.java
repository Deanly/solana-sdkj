package net.deanly.solana.sdk.types.legacy;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeightedEndpoint {

    private String url;
    private Integer weight;

}
