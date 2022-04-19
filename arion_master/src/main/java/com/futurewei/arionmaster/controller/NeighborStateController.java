package com.futurewei.arionmaster.controller;

import java.util.List;

import com.futurewei.arionmaster.data.NeighborStateRepository;
import com.futurewei.arionmaster.model.RoutingRule;
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
    public List<RoutingRule> findByVni(@PathVariable("vni") int vni) {
        logger.info(String.format("findByVni({})", vni));
        return repository.findByVni(vni);
    }

    @GetMapping("/{id}")
    public RoutingRule findById(@PathVariable("id") String id) {
        logger.info("findById({})", id);
        return repository.findById(id).get();
    }

    @GetMapping("/hostip/{hostip}")
    public List<RoutingRule> findByHostIp(@PathVariable("hostip") String hostip) {
        logger.info("findByHostIp({})", hostip);
        return repository.findByHostip(hostip);
    }

    @PostMapping
    public RoutingRule add(@RequestBody RoutingRule neighbor) {
        logger.info("add({})", neighbor);
        return repository.save(neighbor);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        logger.info("delete({})", id);
        repository.deleteById(id);
    }
}
