package org.redhat.bpm.workbeat.jms;

import java.io.Serializable;

public interface JMSProducer {

    void closeConnection() throws Exception;

    /**
     * To use from a client outside the broker
     */
    void sendMsgToRemoteBroker(
            Serializable payload,
            String username,
            String password,
            String initialCf,
            String remoteCf,
            String remoteUrl,
            String destinationType,
            String destinationName) throws Exception;

    /**
     * To use from a client colocated with the broker
     */
    void sendMsgToLocalBroker(
            Serializable payload,
            String username,
            String password,
            String localCf,
            String destinationType,
            String destinationName) throws Exception;

    /**
     * To use from a remote client with a IBM MQ broker
     */
    void sendMsgToIBMMQ(
            Serializable payload,
            String username,
            String password,
            String remoteUrl,
            Integer remotePort,
            String channelName,
            String queueManager,
            String destinationName,
            String destinationType,
            String topicObject) throws Exception;
}
