package org.acme.controller;

import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.dto.SaveDmnRequest;

@Path("/api")
public class DmnResource {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/dmn")
    public Response dmnTest(@Context SecurityIdentity identity, SaveDmnRequest saveDmnRequest){
        String dmnModel = saveDmnRequest.dmnModel;

        try {
            Log.info("Received DMN model: " + dmnModel);
            return Response.ok().build();
        } catch (Exception e){
            Log.info(("Failed to test DMN model"));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}