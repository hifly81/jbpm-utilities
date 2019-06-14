package org.redhat.bpm.extensions.command.kafka.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.kie.internal.executor.api.ErrorInfo;

import java.util.Map;

public class ErrorInfoJsonSerializer implements Serializer<ErrorInfo> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public byte[] serialize(String topic, ErrorInfo data) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsString(data).getBytes();
        } catch (Exception exception) {
            System.out.println("Error in serializing object"+ data);
        }
        return retVal;

    }

    @Override
    public void close() {}

}