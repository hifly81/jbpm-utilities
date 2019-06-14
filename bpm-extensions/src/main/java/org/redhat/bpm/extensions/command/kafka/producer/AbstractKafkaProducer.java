package org.redhat.bpm.extensions.command.kafka.producer;

import org.apache.kafka.clients.producer.Producer;

public abstract class AbstractKafkaProducer<K,V> {

    protected Producer<K, V> producer;

    public Producer<K, V> getProducer() {
        return producer;
    }

}
