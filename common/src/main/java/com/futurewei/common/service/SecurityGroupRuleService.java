package com.futurewei.common.service;

import com.futurewei.common.model.SecurityGroupPortBinding;
import com.futurewei.common.model.SecurityGroupRule;
import com.hazelcast.query.Predicate;

import java.util.Map;

public class SecurityGroupRuleService implements Predicate<String, SecurityGroupRule> {
    private String group;
    private long rev;

    public SecurityGroupRuleService(String group, long rev) {
        this.group = group;
        this.rev = rev;
    }

    @Override
    public boolean apply(Map.Entry<String, SecurityGroupRule> entry) {
        var securityGroupRule = entry.getValue();
        if (securityGroupRule.getVersion() >= rev) {
            return true;
        }

        return false;

    }
}
