package com.futurewei.arionmaster.service;

import com.futurewei.arion.schema.Arionmaster;

import java.util.function.Consumer;

public interface Watcher {
    Runnable watch(Arionmaster.ArionWingRequest req, Consumer<Arionmaster.ArionWingResponse> resConsumer);
}
