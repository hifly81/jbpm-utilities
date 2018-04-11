package org.redhat.bpm.migration;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.admin.ProcessAdminServicesClient;

public class MigrationClient {

	private static final String URL = "";
    private static final String USER = "";
	private static final String PASSWORD = "";

	private static final MarshallingFormat FORMAT = MarshallingFormat.JSON;

	private static final String CONTAINER_V1 = "";
	private static final String CONTAINER_V2 = "";

	private static final String P_ID = "";
	private static KieServicesClient client;
	private static ProcessAdminServicesClient processAdminClient;

	public static void main(String[] args) throws Exception {

		String processPid = args[0];
		if(processPid != null) {

			KieServicesConfiguration conf = KieServicesFactory.newRestConfiguration(URL, USER, PASSWORD);
			conf.setTimeout(100000);
			conf.setMarshallingFormat(FORMAT);
			client = KieServicesFactory.newKieServicesClient(conf);
			processAdminClient = client.getServicesClient(ProcessAdminServicesClient.class);
			MigrationReportInstance reportInstance = processAdminClient.migrateProcessInstance(CONTAINER_V1, Long.valueOf(processPid), CONTAINER_V2, P_ID);
		}

	}
	
}