package org.p2p.solanaj.core.message;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.p2p.solanaj.core.PublicKey;

import java.util.List;

@Value
@RequiredArgsConstructor
@ToString
public class LoadedAddresses {
    List<PublicKey> writable;
    List<PublicKey> readonly;
}
