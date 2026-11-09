package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/hello-world")
@Consumes("application/json")
@Produces("application/json")
public class KlamResource {
    @GET
    @Produces("text/plain")
    public String hello() {
        return "Hello, World!";
    }
}