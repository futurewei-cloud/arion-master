package com.futurewei.arionmaster.version;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class VersionManager {

    private IAtomicLong counter;

    public VersionManager() {
        HazelcastInstance instance = HazelcastClient.newHazelcastClient();
        counter = instance.getCPSubsystem().getAtomicLong( "counter" );
    }

    public long getVersion() {
        return counter.incrementAndGet();
    }
}
