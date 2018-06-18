package org.redhat.bpm.util;

public class Constants {

    public static final String KIESERVER_HOSTNAME = "localhost";
    public static final String KIESERVER_PROTOCOL = "http";
    public static final String KIESERVER_CONTEXTPATH = "kie-server";
    public static final int KIESERVER_PORT = 8080;
    public static final int KIESERVER_TIMEOUT = 30000;
    //FIXME define username
    public static final String KIESERVER_USERNAME = "bpmsAdmin";
    //FIXME define password
    public static final String KIESERVER_PASSWORD = "password@1";
    public static final String CONTAINER_ID = "com.redhat.bpm:test-task:1.0";
    public static final String REMOTING_URL = "remote://localhost:4447";
    public static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    public static final String CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    public static final String REQUEST_QUEUE_JNDI = "jms/queue/KIE.SERVER.REQUEST";
    public static final String RESPONSE_QUEUE_JNDI = "jms/queue/KIE.SERVER.RESPONSE";



}
