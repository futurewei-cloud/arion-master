package com.futurewei.arionmaster.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;

public class RoutintRuleSerializer
        implements StreamSerializer<RoutingRule> {

    @Override
    public int getTypeId () {
        return 10;
    }

    @Override
    public void write( ObjectDataOutput out,  RoutingRule routingRule )
            throws IOException {
        out.writeString(routingRule.getId());
        out.writeString(routingRule.getMac());
        out.writeString(routingRule.getHostmac());
        out.writeString(routingRule.getHostip());
        out.writeString(routingRule.getIp());
        out.writeInt(routingRule.getVni());
        out.writeLong(routingRule.getVersion());
    }

    @Override
    public RoutingRule read( ObjectDataInput in )
            throws IOException {
        String id = in.readString();
        String mac = in.readString();
        String hostmac = in.readString();
        String hostip = in.readString();
        String ip = in.readString();
        int vni = in.readInt();
        long version = in.readLong();
        return new RoutingRule(id, mac, hostmac, hostip, ip, vni,version);
    }

    @Override
    public void destroy () {
    }
}
