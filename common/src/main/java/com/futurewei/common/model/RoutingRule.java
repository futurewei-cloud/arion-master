/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.Id;

import java.io.IOException;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class RoutingRule implements IdentifiedDataSerializable, Serializable {

    private static final long serialVersionUID = 6529685098267757690L;

    @Id
    private String id;

    @EqualsAndHashCode.Exclude
    @JsonProperty("mac")
    private String mac;

    @EqualsAndHashCode.Exclude
    @JsonProperty("hostmac")
    private String hostMac;

    @EqualsAndHashCode.Exclude
    @JsonProperty("hostip")
    private String hostIp;

    @EqualsAndHashCode.Exclude
    @JsonProperty("ip")
    private String ip;

    @EqualsAndHashCode.Exclude
    @JsonProperty("vni")
    private int vni;

    @EqualsAndHashCode.Exclude
    private long version;

    public RoutingRule() {

    }

    public RoutingRule(String id, String mac, String hostMac, String hostIp, String ip, int vni, long version) {
        this.id = id;
        this.mac = mac;
        this.hostMac = hostMac;
        this.hostIp = hostIp;
        this.ip = ip;
        this.vni = vni;
        this.version = version;
    }

    @Override
    public int getFactoryId() {
        return ArionDataSerializableFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return ArionDataSerializableFactory.ROUTING_RULE_TYPE;
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeString(id);
        objectDataOutput.writeString(mac);
        objectDataOutput.writeString(hostMac);
        objectDataOutput.writeString(hostIp);
        objectDataOutput.writeString(ip);
        objectDataOutput.writeInt(vni);
        objectDataOutput.writeLong(version);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.id = objectDataInput.readString();
        this.mac = objectDataInput.readString();
        this.hostMac = objectDataInput.readString();
        this.hostIp = objectDataInput.readString();
        this.ip = objectDataInput.readString();
        this.vni = objectDataInput.readInt();
        this.version = objectDataInput.readLong();
    }
}
