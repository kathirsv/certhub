package com.certhub.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;

@Path("/view")
public class ViewResource {

    @GET
    @Path("/{shareableId}")
    @Produces(MediaType.TEXT_HTML)
    public Response viewCertificate(@PathParam("shareableId") String shareableId) {
        // Serve the index.html file for client-side routing
        InputStream indexHtml = getClass().getClassLoader().getResourceAsStream("META-INF/resources/index.html");
        if (indexHtml != null) {
            return Response.ok(indexHtml).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}