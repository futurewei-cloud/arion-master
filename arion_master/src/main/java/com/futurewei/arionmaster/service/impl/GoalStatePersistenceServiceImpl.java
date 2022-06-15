/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.arionmaster.service.impl;

import com.futurewei.alcor.schema.Common;
import com.futurewei.common.model.NeighborRule;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import com.futurewei.arionmaster.data.NeighborStateRepository;
import com.futurewei.arionmaster.service.GoalStatePersistenceService;
import com.futurewei.arionmaster.version.VersionManager;
import com.hazelcast.jet.datamodel.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class GoalStatePersistenceServiceImpl implements GoalStatePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(GoalStatePersistenceServiceImpl.class);

    @Autowired
    private VersionManager versionManager;

    @Autowired
    private NeighborStateRepository repository;

    public void goalstateProcess(Goalstateprovisioner.NeighborRulesRequest neighborRulesRequest) throws Exception {

        var version = versionManager.getVersion();
        var neighborStates = getNeighborStateList(neighborRulesRequest, version);
        if (neighborStates.f0().size() > 0) updateNeighborState(neighborStates.f0());
        if (neighborStates.f1().size() > 0) deleteNeighborState(neighborStates.f1());
    }

    private Tuple2<List<NeighborRule>, List<NeighborRule>> getNeighborStateList(Goalstateprovisioner.NeighborRulesRequest neighborRulesRequest, long version) throws Exception{
        List<NeighborRule> neighborStateUpdateList = new ArrayList<>();
        List<NeighborRule> neighborStateDeleteList = new ArrayList<>();
        AtomicReference<Common.OperationType> operationType = new AtomicReference<>(Common.OperationType.INFO);
        neighborRulesRequest.getNeigborstatesList()
                .forEach(neighborState -> {
                    operationType.set(neighborState.getOperationType());
                    neighborState.getConfiguration().getFixedIpsList().forEach(fixedIp -> {
                        NeighborRule neighborRule = new NeighborRule(
                                String.join("-", String.valueOf(fixedIp.getTunnelId()), fixedIp.getIpAddress()),
                                fixedIp.getMacAddress(),
                                fixedIp.getArionGroup(),
                                neighborState.getConfiguration().getMacAddress(),
                                neighborState.getConfiguration().getHostIpAddress(),
                                fixedIp.getIpAddress(),
                                fixedIp.getTunnelId(),
                                version
                        );
                        if (operationType.get().equals(Common.OperationType.CREATE)   ||
                                operationType.get().equals(Common.OperationType.INFO) ||
                                operationType.get().equals(Common.OperationType.UPDATE)
                        ) {
                            neighborStateUpdateList.add(neighborRule);
                        } else if (operationType.get().equals(Common.OperationType.DELETE)) {
                            neighborStateDeleteList.add(neighborRule);
                        }
                    });
                });
        return Tuple2.tuple2(neighborStateUpdateList, neighborStateDeleteList);
    }

    private void updateNeighborState (List<NeighborRule> neighborStateList) {
        try {
            repository.saveAll(neighborStateList);
        } catch (Exception e) {
            throw e;
        }
    }

    private void deleteNeighborState (List<NeighborRule> neighborStateList) {
        try {
            repository.deleteAll(neighborStateList);
        } catch (Exception e) {
            throw e;
        }
    }
}
