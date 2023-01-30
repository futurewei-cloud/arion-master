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
public class SecurityGroupPortBinding implements IdentifiedDataSerializable, Serializable {
    private static final long serialVersionUID = 6529685098267757690L;

    @Id
    private String id;

    @EqualsAndHashCode.Exclude
    @JsonProperty("portid")
    private String portId;

    @EqualsAndHashCode.Exclude
    @JsonProperty("securitygroupid")
    private String securityGroupId;

    @EqualsAndHashCode.Exclude
    @JsonProperty("version")
    private long version;

    @Override
    public int getFactoryId() {
        return ArionDataSerializableFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return ArionDataSerializableFactory.SECURITY_GROUP_PORT_BINDING;
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeString(id);
        objectDataOutput.writeString(portId);
        objectDataOutput.writeString(securityGroupId);
        objectDataOutput.writeLong(version);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.id = objectDataInput.readString();
        this.portId = objectDataInput.readString();
        this.securityGroupId = objectDataInput.readString();
        this.version = objectDataInput.readLong();
    }
}
