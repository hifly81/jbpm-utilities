package org.redhat.bpm.workbeat.service;

public class KieServiceFactory {

    public static KieService create(String clientMode, GatewaySettings settings) {
        switch (clientMode) {
            case "Rest":
                return new KieServiceHTTP(settings);
            case "JMS":
                return new KieServiceJMS(settings);
            default:
                return new KieServiceHTTP(settings);
        }
    }
}
