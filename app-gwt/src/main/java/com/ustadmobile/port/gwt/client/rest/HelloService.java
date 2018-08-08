package com.ustadmobile.port.gwt.client.rest;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;
import javax.ws.rs.DELETE;	
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello-service")
public interface HelloService extends RestService {
	
	@POST
    //@Path("/listen")
    public void listen(Listen request, 
                      MethodCallback<UMMessage> callback);
	
	@GET
	@Path("/say")
	@Produces(MediaType.APPLICATION_JSON)
	public void say(MethodCallback<String> callback);
	
	@DELETE
    @Path("/ping")
    void ping(MethodCallback<Void> callback);
	
}
