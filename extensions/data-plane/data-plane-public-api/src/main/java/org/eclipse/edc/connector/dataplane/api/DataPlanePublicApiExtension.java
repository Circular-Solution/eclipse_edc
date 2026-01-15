package org.eclipse.edc.connector.dataplane.api;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.edc.connector.dataplane.spi.Endpoint;
import org.eclipse.edc.connector.dataplane.spi.iam.DataPlaneAuthorizationService;
import org.eclipse.edc.connector.dataplane.spi.iam.PublicEndpointGeneratorService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.PortMapping;
import org.eclipse.edc.web.spi.configuration.PortMappingRegistry;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;

@Extension("DataPlane Public API Extension")
public class DataPlanePublicApiExtension implements ServiceExtension {

    private static final String PUBLIC_ENDPOINT_URL = "edc.dataplane.api.public.baseurl";
    private static final String PUBLIC_API_CONTEXT = "public";

    @Inject
    private PublicEndpointGeneratorService generatorService;

    @Inject
    private Vault vault;

    @Inject
    private WebService webService;

    @Inject
    private DataPlaneAuthorizationService authorizationService;

    @Inject
    private PortMappingRegistry portMappingRegistry;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var publicEndpoint = context.getSetting(PUBLIC_ENDPOINT_URL, "http://localhost:8185/public");
        var publicPort = context.getSetting("web.http.public.port", 8185);
        var publicPath = context.getSetting("web.http.public.path", "/public");

        portMappingRegistry.register(new PortMapping(PUBLIC_API_CONTEXT, publicPort, publicPath));

        generatorService.addGeneratorFunction("HttpData", address -> Endpoint.url(publicEndpoint));
        generatorService.addResponseGeneratorFunction("HttpData", () -> Endpoint.url(publicEndpoint + "/responseChannel"));

        try {
            var key = new ECKeyGenerator(Curve.P_256)
                .keyID("private-key")
                .generate();
            vault.storeSecret("private-key", key.toJSONString());
            vault.storeSecret("public-key", key.toPublicJWK().toJSONString());
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate signing keys", e);
        }

        webService.registerResource(PUBLIC_API_CONTEXT, new PublicApiController(authorizationService));
    }

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public static class PublicApiController {

        private final DataPlaneAuthorizationService authorizationService;
        private final HttpClient httpClient;

        public PublicApiController(DataPlaneAuthorizationService authorizationService) {
            this.authorizationService = authorizationService;
            this.httpClient = HttpClient.newHttpClient();
        }

        @GET
        @Path("{any:.*}")
        public Response getData(@Context ContainerRequestContext requestContext) {
            var token = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (token == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"No authorization header\"}")
                    .build();
            }

            var result = authorizationService.authorize(token, emptyMap());
            if (result.failed()) {
                var error = result.getFailureDetail() != null ? result.getFailureDetail() : "Authorization failed";
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"" + error + "\"}")
                    .build();
            }

            var sourceAddress = result.getContent();
            var baseUrl = sourceAddress.getStringProperty(EDC_NAMESPACE + "baseUrl");
            if (baseUrl == null) {
                baseUrl = sourceAddress.getStringProperty("baseUrl");
            }
            if (baseUrl == null) {
                baseUrl = sourceAddress.getStringProperty("https://w3id.org/edc/v0.0.1/ns/baseUrl");
            }

            if (baseUrl == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"No baseUrl found. Properties: " + sourceAddress.getProperties() + "\"}")
                    .build();
            }

            try {
                var request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .GET()
                    .build();

                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return Response.status(response.statusCode())
                    .entity(response.body())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getClass().getName() + ": " + e.getMessage() + "\"}")
                    .build();
            }
        }
    }
}
