package com.futurewei.arionmaster.model;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.Id;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class RoutingRule implements Serializable {

    @Id
    private String id;
    @EqualsAndHashCode.Exclude
    private String mac;
    @EqualsAndHashCode.Exclude
    private String hostip;
    @EqualsAndHashCode.Exclude
    private String ip;
    @EqualsAndHashCode.Exclude
    private int vni;
    @EqualsAndHashCode.Exclude
    private long version;


    public RoutingRule(String id, String mac, String hostip, String ip, int vni, long version) {
        this.id = id;
        this.mac = mac;
        this.hostip = hostip;
        this.ip = ip;
        this.vni = vni;
        this.version = version;
    }

}
