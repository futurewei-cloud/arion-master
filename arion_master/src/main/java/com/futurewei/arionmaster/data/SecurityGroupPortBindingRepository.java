package com.futurewei.arionmaster.data;

import com.futurewei.common.model.SecurityGroupPortBinding;
import com.futurewei.common.model.SecurityGroupRule;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SecurityGroupPortBindingRepository extends CrudRepository<SecurityGroupPortBinding, String> {

    List<SecurityGroupPortBinding> findSecurityGroupPortBindingByPortId(String portid);

    List<SecurityGroupPortBinding> findSecurityGroupPortBindingBySecurityGroupId(String securitygroupid);

}
