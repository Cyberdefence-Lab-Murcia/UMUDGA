package es.um.dga.restful.pages;

import com.google.common.net.InternetDomainName;
import es.um.dga.features.nlp.Analyzer;
import es.um.dga.features.nlp.utils.Feature;
import es.um.dga.features.utils.DateHelper;
import es.um.dga.features.utils.Settings;
import es.um.dga.restful.DGAApplication;
import es.um.dga.restful.models.FQDN;
import es.um.dga.restful.models.FQDNFeaturesFilter;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Process page.
 */
@Path("domain")
public class DomainAnalyzer {

    /**
     * Feature cache.
     */
    HashMap<String, Collection<Feature>> cache = new HashMap<>();

    /**
     * Return the web page for the home.
     *
     * @return a text/plain page.
     */
    @Path("features")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doProcessDomain(FQDN fqdn) {
        double cpu_time = System.nanoTime();
        double wall_time = System.currentTimeMillis();
        boolean cached = false;

        Collection<Feature> allFeatures;
        if (this.cache.containsKey(fqdn.getFqdn())) {
            allFeatures = this.cache.get(fqdn.getFqdn());
            cached = true;
        } else {
            Analyzer analyzer = new Analyzer(InternetDomainName.from(fqdn.getFqdn()));
            analyzer.computeAllFeatures();

            allFeatures = analyzer.getAllFeatures();
            this.cache.put(fqdn.getFqdn(), allFeatures);
        }

        cpu_time = (System.nanoTime()-cpu_time)/1000000;
        wall_time = System.currentTimeMillis() - wall_time;

        return Response.accepted().entity(allFeatures)
                .header("cpu-time-ms", cpu_time)
                .header("wall-time-ms", wall_time)
                .header("cached", cached)
                .build();
    }

    /**
     * Return the web page for the home.
     *
     * @return a text/plain page.
     */
    @Path("features/filtered")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doProcessDomainFiltered(FQDNFeaturesFilter fqdn) {
        
        double cpu_time = System.nanoTime();
        double wall_time = System.currentTimeMillis();

        Analyzer analyzer = new Analyzer(InternetDomainName.from(fqdn.getFqdn()));
        analyzer.computeSomeFeatures(fqdn.getFeatures());

        Collection<Feature> allFeatures = analyzer.getAllFeatures();

        cpu_time = (System.nanoTime()-cpu_time)/1000000;
        wall_time = System.currentTimeMillis() - wall_time;

        return Response.accepted().entity(allFeatures)
                .header("cpu-time-ms", cpu_time)
                .header("wall-time-ms", wall_time)
                .build();
    }

    @Path("classify")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doClassifyDomain(FQDN fqdn) {
        LocalDateTime start = LocalDateTime.now();
        try {
            fqdn = DGAApplication.binaryClassifier.classify(fqdn);
            return Response.accepted().entity(fqdn).header("time", DateHelper.humanReadableDifference(start)).build();
        } catch (Exception ex) {
            Settings.getLogger().log(Level.SEVERE, "Classifier error!", ex);
            return Response.serverError().entity(ex).header("time", DateHelper.humanReadableDifference(start)).build();
        }
    }
}
