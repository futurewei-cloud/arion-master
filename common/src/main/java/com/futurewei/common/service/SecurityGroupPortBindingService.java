package com.futurewei.common.service;

import com.futurewei.common.model.SecurityGroupPortBinding;
import com.hazelcast.query.Predicate;

import java.util.Map;

public class SecurityGroupPortBindingService implements Predicate<String, SecurityGroupPortBinding> {
    private String group;
    private long rev;

    public SecurityGroupPortBindingService(String group, long rev) {
        this.group = group;
        this.rev = rev;
    }

    @Override
    public boolean apply(Map.Entry<String, SecurityGroupPortBinding> entry) {
        var securityGroupPortBinding = entry.getValue();
        if (securityGroupPortBinding.getVersion() >= rev) {
            return true;
        }

        return false;

    }
}
