/*
 * Copyright (C) 2018-2021 Authlete, Inc.
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
package com.authlete.jaxrs.server.api.openbanking;


import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.util.Utils;
import com.authlete.jaxrs.AccessTokenInfo;
import com.authlete.jaxrs.AccessTokenValidator.Params;
import com.authlete.jaxrs.BaseResourceEndpoint;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * This is a dummy implementation of {@code /account-access-consents} API
 * which is defined in the specification of KSA / SAMA Open Banking.
 */
@Path("/api/open-banking/v1.1/account-access-consents")
public class KSAAccountAccessConsentsEndpoint extends BaseResourceEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @HeaderParam("x-fapi-interaction-id") @DefaultValue("") String incomingInteractionId,
            @Context HttpServletRequest request)
    {
        // Process the access token.
        AccessTokenInfo atInfo = processAccessToken(authorization, request);

        // Prepare the content of the response.
        Map<String, Object> content = buildContent(atInfo);

        // Prepare the outgoing interaction ID.
        String outgoingInteractionId = buildInteractionId(incomingInteractionId);

        // Build the response.
        return buildResponse(content, outgoingInteractionId);
    }


    private AccessTokenInfo processAccessToken(String authorization, HttpServletRequest request)
    {
        // Extract an access token from the Authorization header.
        String accessToken = extractAccessToken(authorization, null);

        // Extract a client certificate.
        String certificate = extractClientCertificate(request);

        // If the request does not contain an access token.
        if (accessToken == null)
        {
            // Hmm. This should not happen in production environments.
            return null;
        }

        // Parameters for access token validation.
        Params params = new Params().setAccessToken(accessToken).setClientCertificate(certificate);

        // Validate the access token.
        return validateAccessToken(AuthleteApiFactory.getDefaultApi(), params);
    }


    private Map<String, Object> buildContent(AccessTokenInfo atInfo)
    {
        // {
        //   "Data" : {
        //     "ConsentId" : "<string>"
        //   }
        // }

        Map<String, Object> content = new HashMap<String, Object>();

        // Data
        Map<String, Object> data = new HashMap<String, Object>();
        content.put("Data", data);

        // Generate an account request ID.
        String accountRequestId = generateAccountRequestId(atInfo);

        data.put("ConsentId", accountRequestId);

        return content;
    }


    private String generateAccountRequestId(AccessTokenInfo atInfo)
    {
        String random = UUID.randomUUID().toString();

        // If information about the access token is not available.
        if (atInfo == null)
        {
            return random;
        }

        // Prepend "{ClientId}:".
        return String.format("%d:%s", atInfo.getClientId(), random);
    }


    private String buildInteractionId(String incomingInteractionId)
    {
        if (incomingInteractionId != null && !incomingInteractionId.isEmpty())
        {
            // Embed the same interaction ID in the response.
            return incomingInteractionId;
        }

        // Generate a new interaction ID.
        return UUID.randomUUID().toString();
    }


    private Response buildResponse(Map<String, Object> content, String interactionId)
    {
        // 201 Created, application/json
        return Response
                .status(Status.CREATED)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Utils.toJson(content, true))
                .header("x-fapi-interaction-id", interactionId)
                .build();
    }
}
