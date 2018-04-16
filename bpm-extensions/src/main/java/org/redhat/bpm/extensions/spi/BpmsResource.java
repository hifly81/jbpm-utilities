package org.redhat.bpm.extensions.spi;

import org.jbpm.services.api.DeploymentNotFoundException;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.text.MessageFormat;

import static org.kie.server.remote.rest.common.util.RestUtils.*;

@Path("ext/server/containers/{id}/processes")
public class BpmsResource {
	
	public static final Logger logger = LoggerFactory.getLogger(BpmsResource.class);
	
	private final BpmsService bpmsService;
	private final KieServerRegistry context;
	
	public BpmsResource(BpmsService bpmsService, KieServerRegistry context) {
		this.bpmsService = bpmsService;
		this.context = context;
	}

    @DELETE
    @Path("instances/{pInstanceId}/timer")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response cancelTimer(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
                                          @PathParam("pInstanceId") Long processInstanceId, String eventPayload) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            bpmsService.cancelTimer(containerId, processInstanceId, null, null);

            return createResponse(null, v, Response.Status.OK, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format("Could not find process instance with id \"{0}\"", processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format("Could not find container \"{0}\"", containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format("Unexpected error during processing: {0}", e.getMessage()), v, conversationIdHeader);
        }
    }

    @POST
    @Path("instances/{pInstanceId}/timer/trigger")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response triggerTimer(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
                                @PathParam("pInstanceId") Long processInstanceId, String eventPayload) {

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            bpmsService.triggerTimer(containerId, processInstanceId, null, null);

            return createResponse(null, v, Response.Status.OK, conversationIdHeader);

        } catch (ProcessInstanceNotFoundException e) {
            return notFound(MessageFormat.format("Could not find process instance with id \"{0}\"", processInstanceId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(MessageFormat.format("Could not find container \"{0}\"", containerId), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(MessageFormat.format("Unexpected error during processing: {0}", e.getMessage()), v, conversationIdHeader);
        }
    }

}
