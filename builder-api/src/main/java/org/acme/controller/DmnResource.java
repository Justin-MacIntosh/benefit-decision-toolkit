package org.acme.controller;

import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.*;

import org.acme.service.DmnService;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNRuntime;


@Path("/api")
public class DmnResource {
    @Inject
    DmnService dmnService;

    private KieSession initializeKieSession(byte[] moduleBytes) throws IOException {
        KieServices kieServices = KieServices.Factory.get();
        Resource jarResource = kieServices.getResources().newByteArrayResource(moduleBytes);
        KieModule kieModule = kieServices.getRepository().addKieModule(jarResource);

        ReleaseId releaseId = kieModule.getReleaseId();
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        return kieContainer.newKieSession();
    }

    public String decompressDmnXml(byte[] compressedDmn) {
        try {
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressedDmn));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
            String line;
            StringBuilder outStr = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                outStr.append(line);
            }
            return outStr.toString();
        } catch (Exception e){
            Log.info(("Failed to decompress DMN model"));
            return null;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/dmn")
    public Response dmnTest(@Context SecurityIdentity identity, byte[] compressedDmn) {
        try {
            Log.info("Size of received DMN model (compressed): " + compressedDmn.length + " bytes");
            String dmnXml = decompressDmnXml(compressedDmn);
            Log.info("Size of received DMN model (decompressed): " + dmnXml.length() + " characters");

            HashMap<String, String> dmnDependenciesMap = new HashMap<String, String>();
            byte[] compiledDmn = dmnService.compileDmnModel(dmnXml, dmnDependenciesMap, "test-model");
            
            KieSession kieSession = initializeKieSession(compiledDmn);
            DMNRuntime dmnRuntime = kieSession.getKieRuntime(DMNRuntime.class);

            List<DMNModel> dmnModels = dmnRuntime.getModels();
            Log.info("Number of DMN models: " + dmnModels.size());
            for (DMNModel model : dmnModels) {
                model.getItemDefinitions().forEach(itemDef -> {
                    Log.info("Person def: " + itemDef.getType().getFields());
                    Log.info(" - Item Definition: " + itemDef.getName());
                });
                Log.info("DMN Model found: " + model.getName());
                model.getInputs().forEach(input -> {
                    Log.info(" - Input: " + input.getName());
                });
            }

            Log.info("Received DMN model: " + dmnXml);
            return Response.ok().build();
        } catch (Exception e){
            Log.info("Failed to test DMN model: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
