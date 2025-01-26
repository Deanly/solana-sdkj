package net.deanly.solanarpcj.rpc.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LeaderSchedule {
    private final String identity;
    private final List<Double> slotIndexes;
}
