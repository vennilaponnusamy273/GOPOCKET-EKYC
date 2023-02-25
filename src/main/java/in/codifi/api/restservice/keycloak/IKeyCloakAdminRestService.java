package in.codifi.api.restservice.keycloak;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import in.codifi.api.model.CreateUserRequestModel;

@RegisterRestClient(configKey = "auth-user-api")
@RegisterClientHeaders
public interface IKeyCloakAdminRestService {
	/**
	 * 
	 * @param authHeader
	 * @param user
	 */
	@Path("/users")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Create a new user after phone, email and mpin registration")
	public void addNewUser(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader, CreateUserRequestModel user);
}
