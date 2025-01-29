package net.deanly.solanarpcj.transaction.message.meta;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import net.deanly.solanarpcj.crypto.PublicKey;

import java.util.List;

@Value
@RequiredArgsConstructor
@ToString
public class LoadedAddresses {
    List<PublicKey> writable;
    List<PublicKey> readonly;
}
