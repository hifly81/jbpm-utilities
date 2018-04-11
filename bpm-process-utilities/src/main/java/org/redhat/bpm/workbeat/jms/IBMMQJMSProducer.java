package org.redhat.bpm.workbeat.jms;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;

import javax.jms.IllegalStateException;
import java.io.Serializable;

/**
 * based on https://stackoverflow.com/questions/45638710/two-simple-ibm-mq-client-tests-write-to-mq-queue-why-does-one-work-but-not-t
 **/
public class IBMMQJMSProducer implements JMSProducer {

    private MQDestination sender = null;
    private MQQueueManager qMgr = null;

    public void sendMsgToIBMMQ(
            Serializable payload,
            String username,
            String password,
            String remoteUrl,
            Integer remotePort,
            String channelName,
            String queueManager,
            String destinationName,
            String destinationType,
            String topicObject) throws Exception {

        int openOptions = CMQC.MQOO_INQUIRE | CMQC.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT;

        MQEnvironment.hostname = remoteUrl;
        MQEnvironment.port = remotePort;
        MQEnvironment.channel = channelName;
        if (username != null && password != null) {
            MQEnvironment.properties.put(CMQC.USER_ID_PROPERTY, username);
            MQEnvironment.properties.put(CMQC.PASSWORD_PROPERTY, password);
        }
        MQEnvironment.properties.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES);
        qMgr = new MQQueueManager(queueManager);

        if (destinationType.equalsIgnoreCase("queue")) {
            sender = qMgr.accessQueue(destinationName, openOptions);
        }
        else {
            //Topic name: my_new_topic
            //Topic string: my_new_string
            sender = qMgr.accessTopic(destinationName, topicObject, CMQC.MQTOPIC_OPEN_AS_PUBLICATION, CMQC.MQOO_OUTPUT);
        }


        MQMessage mqMessage = new MQMessage();
        mqMessage.writeObject(payload);
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        sender.put(mqMessage, pmo);

        closeConnection();
    }

    public void closeConnection() throws Exception {
        sender.close();
        qMgr.disconnect();
    }

    @Override
    public void sendMsgToRemoteBroker(Serializable payload, String username, String password, String initialCf, String remoteCf, String remoteUrl, String destinationType, String destinationName) throws Exception {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void sendMsgToLocalBroker(Serializable payload, String username, String password, String localCf, String destinationType, String destinationName) throws Exception {
        throw new IllegalStateException("Not implemented");
    }


}