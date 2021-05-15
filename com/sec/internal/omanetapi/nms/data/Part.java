package com.sec.internal.omanetapi.nms.data;

public class Part {
    public String comm_addr;
    public String name;
    public String role;

    public String toString() {
        return "name=" + this.name + ";comm_addr=" + this.comm_addr + ";role=" + this.role;
    }
}
