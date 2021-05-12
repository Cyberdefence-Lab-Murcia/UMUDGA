package es.um.dga.restful.pages;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Main page, it will have path "/".
 */
@Path("/")
public class Main {

    /**
     * Return the web page for the home.
     * @return a text/plain page.
     */
    @GET
    @Produces("text/plain")
    public String getClichedMessage() {
        // Return some cliched textual content
        return "The application is working";
    }
}