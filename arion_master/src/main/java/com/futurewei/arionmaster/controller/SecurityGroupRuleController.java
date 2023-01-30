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
package com.futurewei.arionmaster.controller;

import com.futurewei.arionmaster.data.SecurityGroupPortBindingRepository;
import com.futurewei.arionmaster.data.SecurityGroupRuleRepository;
import com.futurewei.common.model.SecurityGroupPortBinding;
import com.futurewei.common.model.SecurityGroupRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/securitygrouprules")
public class SecurityGroupRuleController {
    private static final Logger logger = LoggerFactory.getLogger(SecurityGroupRuleController.class);

    private SecurityGroupRuleRepository repository;

    SecurityGroupRuleController(SecurityGroupRuleRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/securitygroupid/{securitygroupid}")
    public List<SecurityGroupRule> findBySecurityGroupId(@PathVariable("securitygroupid") String securitygroupid) {
        logger.info(String.format("findBySecurityGroupId({})", securitygroupid));
        return repository.findBySecurityGroupId(securitygroupid);
    }

    @GetMapping("/{id}")
    public SecurityGroupRule findById(@PathVariable("id") String id) {
        logger.info("findById({})", id);
        return repository.findById(id).get();
    }

    @GetMapping("/remotegroupid/{remotegroupid}")
    public List<SecurityGroupRule> findByHostIp(@PathVariable("remotegroupid") String remotegroupid) {
        logger.info("findByRemoteGroupId({})", remotegroupid);
        return repository.findByRemoteGroupId(remotegroupid);
    }

    @PostMapping
    public SecurityGroupRule add(@RequestBody SecurityGroupRule securityGroupRule) {
        logger.info("add({})", securityGroupRule);
        return repository.save(securityGroupRule);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        logger.info("delete({})", id);
        repository.deleteById(id);
    }
}
