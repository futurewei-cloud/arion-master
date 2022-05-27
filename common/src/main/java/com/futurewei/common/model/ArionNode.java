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

public class ArionNode implements IdentifiedDataSerializable, Serializable {

    private static final long serialVersionUID = 6529685098267757690L;

    @Id
    @JsonProperty("zgc_id")
    private String id;

    @EqualsAndHashCode.Exclude
    @JsonProperty("description")
    private String description;

    @EqualsAndHashCode.Exclude
    @JsonProperty("name")
    private String name;

    @EqualsAndHashCode.Exclude
    @JsonProperty("ip_control")
    private String ipControl;

    @EqualsAndHashCode.Exclude
    @JsonProperty("id_control")
    private String idControl;

    @EqualsAndHashCode.Exclude
    @JsonProperty("pwd_control")
    private String pwdControl;

    @EqualsAndHashCode.Exclude
    @JsonProperty("inf_tenant")
    private String infTenant;

    @EqualsAndHashCode.Exclude
    @JsonProperty("mac_tenant")
    private String macTenant;

    @EqualsAndHashCode.Exclude
    @JsonProperty("inf_zgc")
    private String infZgc;

    @EqualsAndHashCode.Exclude
    @JsonProperty("mac_zgc")
    private String macZgc;


    @Override
    public int getFactoryId() {
        return ArionDataSerializableFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return ArionDataSerializableFactory.ARION_NODE_TYPE;
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeString(id);
        objectDataOutput.writeString(description);
        objectDataOutput.writeString(name);
        objectDataOutput.writeString(ipControl);
        objectDataOutput.writeString(idControl);
        objectDataOutput.writeString(pwdControl);
        objectDataOutput.writeString(infTenant);
        objectDataOutput.writeString(macTenant);
        objectDataOutput.writeString(infZgc);
        objectDataOutput.writeString(macZgc);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.id = objectDataInput.readString();
        this.description = objectDataInput.readString();
        this.name = objectDataInput.readString();
        this.ipControl = objectDataInput.readString();
        this.idControl = objectDataInput.readString();
        this.pwdControl = objectDataInput.readString();
        this.infTenant = objectDataInput.readString();
        this.macTenant = objectDataInput.readString();
        this.infZgc = objectDataInput.readString();
        this.macZgc = objectDataInput.readString();
    }
}
