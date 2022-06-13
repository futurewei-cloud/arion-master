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

import java.util.List;

import com.futurewei.arionmaster.data.NeighborStateRepository;
import com.futurewei.common.model.NeighborRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/neighborstate")
public class NeighborStateController {

    private static final Logger logger = LoggerFactory.getLogger(NeighborStateController.class);

    private NeighborStateRepository repository;

    NeighborStateController(NeighborStateRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/vni/{vni}")
    public List<NeighborRule> findByVni(@PathVariable("vni") int vni) {
        logger.info(String.format("findByVni({})", vni));
        return repository.findByVni(vni);
    }

    @GetMapping("/{id}")
    public NeighborRule findById(@PathVariable("id") String id) {
        logger.info("findById({})", id);
        return repository.findById(id).get();
    }

    @GetMapping("/hostip/{hostip}")
    public List<NeighborRule> findByHostIp(@PathVariable("hostip") String hostip) {
        logger.info("findByHostIp({})", hostip);
        return repository.findByHostIp(hostip);
    }

    @PostMapping
    public NeighborRule add(@RequestBody NeighborRule neighbor) {
        logger.info("add({})", neighbor);
        return repository.save(neighbor);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        logger.info("delete({})", id);
        repository.deleteById(id);
    }
}
