package org.redhat.bpm.workbeat.mdb;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KieService {

    private static final Logger LOG = LoggerFactory.getLogger(KieService.class);

    protected KieServicesConfiguration config;

    protected KieServicesClient client;


    public KieService() {

        String serverUrl = new StringBuilder(Constants.KIESERVER_PROTOCOL)
                .append("://").append(Constants.KIESERVER_HOSTNAME)
                .append(":").append(Constants.KIESERVER_PORT)
                .append("/").append(Constants.KIESERVER_CONTEXTPATH)
                .append("/services/rest/server")
                .toString();

        LOG.info("Server Url {}", serverUrl);

        config = KieServicesFactory.newRestConfiguration(serverUrl, Constants.KIESERVER_USERNAME, Constants.KIESERVER_PASSWORD);
        config.setMarshallingFormat(MarshallingFormat.JSON);
        config.setTimeout(Constants.KIESERVER_TIMEOUT);

    }

    public void startConverasation() {
        client = KieServicesFactory.newKieServicesClient(config);
    }

    public void endConversation() {
        client.completeConversation();
    }

    public void signal(long pi, String containerId, String signalName, String event) {
        client.getServicesClient(ProcessServicesClient.class).signalProcessInstance(containerId, pi, signalName, event);
    }









}
