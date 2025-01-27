package net.deanly.solanarpcj.layout;

import net.deanly.structlayout.StructLayout;

public abstract class Struct {

    public byte[] serialize() {
        return StructLayout.encode(this);
    }

}
