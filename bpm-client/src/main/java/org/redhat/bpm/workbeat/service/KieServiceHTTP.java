package org.redhat.bpm.workbeat.service;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KieServiceHTTP extends KieService {

    private static final Logger LOG = LoggerFactory.getLogger(KieServiceHTTP.class);

    public KieServiceHTTP(GatewaySettings settings) {

        String serverUrl = new StringBuilder(settings.getProtocol())
                .append("://").append(settings.getHostname())
                .append(":").append(settings.getPort())
                .append("/").append(settings.getContextPath())
                .append("/services/rest/server")
                .toString();

        LOG.info("Server Url {}", serverUrl);

        config = KieServicesFactory.newRestConfiguration(serverUrl, settings.getUsername(), settings.getPassword());
        config.setMarshallingFormat(MarshallingFormat.XSTREAM);
        config.setTimeout(settings.getTimeout());

    }



}
