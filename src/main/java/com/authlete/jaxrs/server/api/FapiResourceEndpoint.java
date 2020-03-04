package com.authlete.jaxrs.server.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseResourceEndpoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Path("/api/fapi/{endpoint: .*}")
public class FapiResourceEndpoint extends BaseResourceEndpoint
{

    /**
     * JSON generator.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // date parser
    SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    
    private static Logger logger = Logger.getLogger(FapiResourceEndpoint.class.getName());
    
    @GET
    public Response get(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @HeaderParam("x-fapi-financial-id") @DefaultValue("") String financialId,
            @HeaderParam("x-fapi-interaction-id") @DefaultValue("") String interactionId,
            @HeaderParam("x-fapi-auth-date") @DefaultValue("") String authDate,
            @HeaderParam("x-fapi-customer-ip-address") @DefaultValue("") String customerIpAddress,
            @Context HttpServletRequest request)
    {
        // Extract an access token from the Authorization header (note we don't pass in the query parameter)
        String token = extractAccessToken(authorization, null);

        return process(token, financialId, interactionId, authDate, customerIpAddress, extractClientCertificate(request));
    }

    private Response process(String accessToken, String financialId, String incomingInteractionId, String authDate, String customerIpAddress, String clientCertificate)
    {
        // Validate the access token. Because this endpoint does not require
        // any scopes, here we use the simplest variant of validateAccessToken()
        // methods which does not take 'requiredScopes' argument. See the JavaDoc
        // of BaseResourceEndpoint (authlete-java-jaxrs) for details.
        //
        // validateAccessToken() throws a WebApplicationException when the given
        // access token is invalid. The response contained in the exception
        // complies with RFC 6750, so you don't have to build the content of
        // WWW-Authenticate header in the error response by yourself.
        //
        // If you want to get information about the access token (e.g. the subject
        // of the user and the scopes associated with the access token), use
        // the object returned from validateAccessToken() method. It is an
        // instance of AccessTokenInfo class. If you want to get information
        // even in the case where validateAccessToken() throws an exception,
        // call AuthleteApi.introspect(IntrospectionRequest) directly.
        validateAccessToken(AuthleteApiFactory.getDefaultApi(), accessToken, null, null, clientCertificate, null, null, null);

        // The access token presented by the client application is valid.

        try
        {
            // log the financial ID
            if (financialId != null && !financialId.isEmpty())
            {
                logger.info("(Legacy) FAPI Financial ID: " + financialId);
            }
            
            String outgoingInteractionId = getInteractionId(incomingInteractionId);
            
            // log the interaction ID
            logger.info("FAPI Interaction ID: " + outgoingInteractionId);
            
            // try parsing the date header if it exists
            if (authDate != null && !authDate.isEmpty())
            {
                // this will throw an exception if the format is wrong
                format.parse(authDate);
                
                logger.info("Auth date: " + authDate);
            }
            
            if (customerIpAddress != null && !customerIpAddress.isEmpty())
            {
                logger.info("IP Address: " + customerIpAddress);
            }
            
            String json = GSON.toJson(new JsonObject());
            
            return Response.ok(json)
                    .header("x-fapi-interaction-id", outgoingInteractionId)
                    .build();
            
        }
        catch (IllegalArgumentException | ParseException e)
        {
            logger.severe(e.getMessage());
            return Response.status(Status.BAD_REQUEST).build();
        }
        
    }

    
    // Get the value for the x-fapi-interaction-id header to return
    private String getInteractionId(String interactionId)
    {
        
        if (interactionId != null && !interactionId.isEmpty())
        {
            // make sure the interaction ID is a UUID; this throws an IllegalArgumentException if it fails
            UUID.fromString(interactionId);
            
            return interactionId;
            
        }
        else
        {
            // return a new random UUID if we didn't get one in
            return UUID.randomUUID().toString();
        }
    }
    
}
