package com.futurewei.arionmaster.data;

import java.util.List;


import com.futurewei.arionmaster.model.RoutingRule;
import org.springframework.data.repository.CrudRepository;

public interface NeighborStateRepository extends CrudRepository<RoutingRule, String> {

    List<RoutingRule> findByVni(Integer vni);

    List<RoutingRule> findByHostip(String hostip);
}
