package org.redhat.bpm.workbeat.mdb;

import org.redhat.bpm.workbeat.wid.ProcessPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.*;

@MessageDriven(name = "ProcessSubscriberMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/bpmnProcessesTopic"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class ProcessSubscriber implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessSubscriber.class);


    public void onMessage(Message rcvMessage) {
        ObjectMessage msg;
        try {
            if (rcvMessage instanceof ObjectMessage) {
                msg = (ObjectMessage) rcvMessage;
                ProcessPayload processPayload = (ProcessPayload) msg.getObject();

                LOG.info("Received Message from topic: " + processPayload);

                KieService kieService = new KieService();
                kieService.startConverasation();

                kieService.signal(processPayload.getProcessId(), processPayload.getContainerId(), processPayload.getEventPublished(), "OK");
                kieService.endConversation();

                LOG.info("Event dispatched: " + processPayload);


            } else {
                LOG.warn("Message of wrong type: " + rcvMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}