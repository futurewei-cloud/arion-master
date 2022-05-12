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
import org.springframework.data.annotation.Id;

import java.io.IOException;
import java.io.Serializable;

public class ArionGatewayCluster implements IdentifiedDataSerializable, Serializable {

    private static final long serialVersionUID = 6529685098267757690L;

    @Id
    @JsonProperty("name")
    private String id;

    @EqualsAndHashCode.Exclude
    @JsonProperty("description")
    private String description;

    @EqualsAndHashCode.Exclude
    @JsonProperty("ip_start")
    private String ipStart;

    @EqualsAndHashCode.Exclude
    @JsonProperty("ip_end")
    private String ipEnd;

    @EqualsAndHashCode.Exclude
    @JsonProperty("port_ibo")
    private int portIbo;

    @EqualsAndHashCode.Exclude
    @JsonProperty("overlay_type")
    private String overlayType;

    @Override
    public int getFactoryId() {
        return ArionDataSerializableFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return ArionDataSerializableFactory.GATEWAY_CLUSTER_TYPE;
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeString(id);
        objectDataOutput.writeString(description);
        objectDataOutput.writeString(ipStart);
        objectDataOutput.writeString(ipEnd);
        objectDataOutput.writeInt(portIbo);
        objectDataOutput.writeString(overlayType);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.id = objectDataInput.readString();
        this.description = objectDataInput.readString();
        this.ipStart = objectDataInput.readString();
        this.ipEnd = objectDataInput.readString();
        this.portIbo = objectDataInput.readInt();
        this.overlayType = objectDataInput.readString();
    }
}
