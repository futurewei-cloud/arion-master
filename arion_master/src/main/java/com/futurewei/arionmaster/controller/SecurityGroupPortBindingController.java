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

import com.futurewei.arionmaster.data.NeighborStateRepository;
import com.futurewei.arionmaster.data.SecurityGroupPortBindingRepository;
import com.futurewei.common.model.NeighborRule;
import com.futurewei.common.model.SecurityGroupPortBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/securitygroupportbinding")
public class SecurityGroupPortBindingController {
    private static final Logger logger = LoggerFactory.getLogger(SecurityGroupPortBindingController.class);

    private SecurityGroupPortBindingRepository repository;

    SecurityGroupPortBindingController(SecurityGroupPortBindingRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/securitygroupid/{securitygroupid}")
    public List<SecurityGroupPortBinding> findBySecurityGroupId(@PathVariable("securitygroupid") String securitygroupid) {
        logger.info(String.format("findSecurityGroupPortBindingBySecurityGroupId({})", securitygroupid));
        return repository.findSecurityGroupPortBindingBySecurityGroupId(securitygroupid);
    }

    @GetMapping("/{id}")
    public SecurityGroupPortBinding findById(@PathVariable("id") String id) {
        logger.info("findById({})", id);
        return repository.findById(id).get();
    }

    @GetMapping("/portid/{portid}")
    public List<SecurityGroupPortBinding> findByPortId(@PathVariable("portid") String portid) {
        logger.info("findSecurityGroupPortBindingByPortId({})", portid);
        return repository.findSecurityGroupPortBindingByPortId(portid);
    }

    @PostMapping
    public SecurityGroupPortBinding add(@RequestBody SecurityGroupPortBinding securityGroupPortBinding) {
        logger.info("add({})", securityGroupPortBinding);
        return repository.save(securityGroupPortBinding);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        logger.info("delete({})", id);
        repository.deleteById(id);
    }
}
