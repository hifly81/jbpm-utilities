package org.redhat.bpm.extensions.command.kafka.producer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class JsonProducer<T> extends AbstractKafkaProducer<String, T> implements BaseKafkaProducer<String, T> {

    private Properties properties;

    public void start(Properties properties) {
        this.properties = properties;
        producer = new org.apache.kafka.clients.producer.KafkaProducer(
                KafkaConfig.jsonProducer(properties.getProperty("valueSerializer")));
    }

    @Override
    public void start(
            Properties properties, KafkaProducer<String, T> kafkaProducer) {
        this.properties = properties;
        producer = kafkaProducer;
    }

    public void stop() {
        producer.close();
    }

    public Future<RecordMetadata> produceFireAndForget(ProducerRecord<String, T> producerRecord) {
        return producer.send(producerRecord);
    }

    public RecordMetadata produceSync(ProducerRecord<String, T> producerRecord) {
        RecordMetadata recordMetadata = null;
        try {
            recordMetadata = producer.send(producerRecord).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return recordMetadata;
    }

    public void sendRecordsSync(List<T> records) {
        for(T record: records) {
            produceSync(new ProducerRecord<>((String)properties.get("topic"), record));
        }

    }

    @Override
    public void produceAsync(ProducerRecord<String, T> producerRecord, Callback callback) {
        producer.send(producerRecord, new BaseProducerCallback());
    }
}


