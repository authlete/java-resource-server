/*
 * Copyright (C) 2018-2024 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package com.authlete.jaxrs.server.api;


import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.IntrospectionRequest;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.hms.SignatureInfo;
import com.authlete.hms.fapi.FapiResourceResponseSigner;
import com.authlete.jaxrs.BaseResourceEndpoint;
import com.authlete.jaxrs.util.RequestUrlResolver;
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

        return process(request, token, financialId, interactionId, authDate, customerIpAddress);
    }


    private Response process(
            HttpServletRequest request,
            String accessToken, String financialId, String incomingInteractionId,
            String authDate, String customerIpAddress)
    {
        // validateAccessToken() throws a WebApplicationException when the given
        // access token is invalid. The response contained in the exception
        // complies with RFC 6750, so you don't have to build the content of
        // WWW-Authenticate header in the error response by yourself.
        IntrospectionRequest  ireq = createIntrospectionRequest(request, accessToken);
        IntrospectionResponse ires = validateAccessToken(AuthleteApiFactory.getDefaultApi(), ireq);

        // The access token presented by the client application is valid.

        try
        {
            // Log the financial ID.
            logFinancialId(financialId);

            // Compute the outgoing x-fapi-interaction-id.
            String outgoingInteractionId = getInteractionId(incomingInteractionId);

            // Log the interaction ID.
            logInteractionId(outgoingInteractionId);

            // Log the date.
            logAuthDate(authDate);

            // Log the customer IP address.
            logCustomerIpAddress(customerIpAddress);

            // Build an HTTP response.
            return buildResponse(request, ires, outgoingInteractionId);
        }
        catch (IllegalArgumentException | ParseException e)
        {
            logger.severe(e.getMessage());
            return Response.status(Status.BAD_REQUEST).build();
        }
    }


    private void logFinancialId(String financialId)
    {
        // log the financial ID
        if (financialId != null && !financialId.isEmpty())
        {
            logger.info("(Legacy) FAPI Financial ID: " + financialId);
        }
    }


    private void logInteractionId(String interactionId)
    {
        logger.info("FAPI Interaction ID: " + interactionId);
    }


    private void logCustomerIpAddress(String customerIpAddress)
    {
        if (customerIpAddress != null && !customerIpAddress.isEmpty())
        {
            logger.info("IP Address: " + customerIpAddress);
        }
    }


    private void logAuthDate(String authDate) throws ParseException
    {
        // try parsing the date header if it exists
        if (authDate != null && !authDate.isEmpty())
        {
            // this will throw an exception if the format is wrong
            format.parse(authDate);

            logger.info("Auth date: " + authDate);
        }
    }


    private IntrospectionRequest createIntrospectionRequest(
            HttpServletRequest request, String accessToken)
    {
        // Resolve the original request URL.
        URI requestUrl = resolveOriginalRequestUrl(request);

        // htu (the original request URL without parameters)
        String htu = String.format("%s://%s%s",
                requestUrl.getScheme(), requestUrl.getAuthority(), requestUrl.getPath());

        // The target URI.
        URI targetUri = requestUrl;

        return new IntrospectionRequest()
                .setToken(accessToken)
                .setClientCertificate(extractClientCertificate(request))
                .setDpop(request.getHeader("DPoP"))
                .setHtm("GET")
                .setHtu(htu)
                .setHeaders(extractHeadersAsPairs(request))
                .setTargetUri(targetUri)
                .setRequestBodyContained(false)
                ;
    }


    private static URI resolveOriginalRequestUrl(HttpServletRequest request)
    {
        // The original request URL.
        return URI.create(new RequestUrlResolver().resolve(request));
    }


    // Get the value for the x-fapi-interaction-id header to return
    private String getInteractionId(String interactionId)
    {
        if (interactionId != null && !interactionId.isEmpty())
        {
            // make sure the interaction ID is a UUID;
            // this throws an IllegalArgumentException if it fails.
            UUID.fromString(interactionId);

            return interactionId;
        }
        else
        {
            // return a new random UUID if we didn't get one in
            return UUID.randomUUID().toString();
        }
    }


    private Response buildResponse(
            HttpServletRequest req, IntrospectionResponse ires, String outgoingInteractionId)
    {
        // The message body of the response.
        String body = GSON.toJson(new JsonObject());

        // A response with "200 OK".
        ResponseBuilder rb = Response.ok(body);

        // Add an "x-fapi-interaction-id" HTTP field.
        rb.header("x-fapi-interaction-id", outgoingInteractionId);

        // If a DPoP nonce is issued.
        if (ires.getDpopNonce() != null)
        {
            // Add a "DPoP-Nonce" HTTP field.
            rb.header("DPoP-Nonce", ires.getDpopNonce());
        }

        // If a response signing is required.
        if (ires.isResponseSigningRequired())
        {
            // Add HTTP fields required for HTTP message signing,
            // such as "Signature" and "Signature-Input".
            processResponseSigning(rb, req, body);
        }

        return rb.build();
    }


    private void processResponseSigning(
            ResponseBuilder rb, HttpServletRequest req, String body)
    {
        // The logic here complies with FAPI 2.0 Message Signing /
        // Resource Response Signing.
        //
        //   FAPI 2.0 Message Signing
        //   https://openid.bitbucket.io/fapi/fapi-2_0-message-signing.html
        //

        // Compute the SHA-256 digest value of the message body.
        byte[] digest = computeSha256(body);

        // The value of the Content-Digest HTTP field (RFC 9530) of the response.
        String contentDigest = String.format(
                "sha-256=:%s:", Base64.getEncoder().encodeToString(digest));

        // Add a "Content-Digest" HTTP field.
        rb.header("Content-Digest", contentDigest);

        // Create a signer.
        FapiResourceResponseSigner signer = new FapiResourceResponseSigner()
                .setMethod(req.getMethod())
                .setTargetUri(resolveOriginalRequestUrl(req))
                .setStatus(200)
                .setResponseContentDigest(contentDigest)
                .setSigningKey(ResponseSigningKey.get())
                ;

        // Signature information.
        SignatureInfo info;

        try
        {
            // Sign the HTTP response.
            info = signer.sign();
        }
        catch (Exception cause)
        {
            System.err.format("Failed to sign the HTTP response: ", cause.getMessage());
            cause.printStackTrace();

            // Give up adding an HTTP message signature.
            return;
        }

        String label = "sig";

        // Construct the value of the Signature-Input HTTP field.
        String signatureInputFieldValue = String.format("%s=%s",
                label, info.getSerializedSignatureMetadata());

        // Add a "Signature-Input" HTTP field.
        rb.header("Signature-Input", signatureInputFieldValue);

        // Construct the value of the Signature HTTP field.
        String signatureFieldValue = String.format("%s=%s",
                label, info.getSerializedSignature());

        // Add a "Signature" HTTP field.
        rb.header("Signature", signatureFieldValue);
    }


    private static byte[] computeSha256(String str)
    {
        try
        {
            // Convert the string into a byte array.
            byte[] input = str.getBytes(StandardCharsets.UTF_8);

            // Compute the SHA-256 digest value of the byte array.
            return MessageDigest.getInstance("SHA-256").digest(input);
        }
        catch (NoSuchAlgorithmException cause)
        {
            // This should not happen.
            System.err.println("SHA-256 is not supported.");

            return null;
        }
    }
}
