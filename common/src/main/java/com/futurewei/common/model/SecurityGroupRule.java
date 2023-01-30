/*
MIT License
Copyright(c) 2022 Futurewei Cloud

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
public class SecurityGroupRule implements IdentifiedDataSerializable, Serializable {

    private static final long serialVersionUID = 6529685098267757690L;

    @Id
    private String id;

    @EqualsAndHashCode.Exclude
    @JsonProperty("securitygroupid")
    private String securityGroupId;

    @EqualsAndHashCode.Exclude
    @JsonProperty("remotegroupid")
    private String remoteGroupId;

    @EqualsAndHashCode.Exclude
    @JsonProperty("direction")
    private String direction;

    @EqualsAndHashCode.Exclude
    @JsonProperty("remoteipprefix")
    private String remoteIpPrefix;

    @EqualsAndHashCode.Exclude
    @JsonProperty("protocol")
    private String protocol;

    @EqualsAndHashCode.Exclude
    @JsonProperty("portrangemax")
    private int portRangeMax;

    @EqualsAndHashCode.Exclude
    @JsonProperty("portrangemin")
    private int portRangeMin;

    @EqualsAndHashCode.Exclude
    @JsonProperty("ethertype")
    private String ethertype;

    @EqualsAndHashCode.Exclude
    @JsonProperty("vni")
    private int vni;

    @EqualsAndHashCode.Exclude
    @JsonProperty("version")
    private long version;

    @Override
    public int getFactoryId() {
        return ArionDataSerializableFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return ArionDataSerializableFactory.SECURITY_GROUP_RULE;
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeString(id);
        objectDataOutput.writeString(securityGroupId);
        objectDataOutput.writeString(remoteGroupId);
        objectDataOutput.writeString(direction);
        objectDataOutput.writeString(remoteIpPrefix);
        objectDataOutput.writeString(protocol);
        objectDataOutput.writeInt(portRangeMax);
        objectDataOutput.writeInt(portRangeMin);
        objectDataOutput.writeString(ethertype);
        objectDataOutput.writeInt(vni);
        objectDataOutput.writeLong(version);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.id = objectDataInput.readString();
        this.securityGroupId = objectDataInput.readString();
        this.remoteGroupId = objectDataInput.readString();
        this.direction = objectDataInput.readString();
        this.remoteIpPrefix = objectDataInput.readString();
        this.protocol = objectDataInput.readString();
        this.portRangeMax = objectDataInput.readInt();
        this.portRangeMin = objectDataInput.readInt();
        this.ethertype = objectDataInput.readString();
        this.vni = objectDataInput.readInt();
        this.version = objectDataInput.readLong();
    }
}
