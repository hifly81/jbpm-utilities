package org.redhat.bpm.workbeat.logHandler;

import org.redhat.bpm.workbeat.jms.GenericJMSProducer;
import org.redhat.bpm.workbeat.jms.JMSProducer;
import org.redhat.bpm.workbeat.wid.ProcessPayload;

import java.time.format.DateTimeFormatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class JmsHandler extends Handler {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String auditEnabled;
    private String localCf;
    private String destinationName;
    private String destinationType;

    public String getAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(String auditEnabled) {
        this.auditEnabled = auditEnabled;
    }

    public String getLocalCf() {
        return localCf;
    }

    public void setLocalCf(String localCf) {
        this.localCf = localCf;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    //Publish a LogRecord.
    public void publish(LogRecord record) {
        JMSProducer jmsProducer = null;
        try {

            if (Boolean.valueOf(auditEnabled)) {

                ProcessPayload payload = new ProcessPayload();
                payload.setErrorDetail(record.getSourceClassName() + " - " + record.getSourceMethodName());

                jmsProducer = new GenericJMSProducer();
                jmsProducer.sendMsgToLocalBroker(payload, null, null, localCf, destinationType, destinationName);


            }
        } catch (Throwable ex2) {
            ex2.printStackTrace();
        } finally {
            if (jmsProducer != null)
                try {
                    jmsProducer.closeConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

    }

    //Flush any buffered output.
    public void flush() { }

    //Close the Handler and free all associated resources.
    public void close() throws SecurityException { }

}