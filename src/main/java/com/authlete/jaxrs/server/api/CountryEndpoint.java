/*
 * Copyright (C) 2016 Authlete, Inc.
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


import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseResourceEndpoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neovisionaries.i18n.CountryCode;


/**
 * An endpoint that returns country code information.
 *
 * <p>
 * The API path is <code>/api/country/<i>{countryCode}</i></code> where
 * <code><i>{countryCode}</i></code> is an ISO 3166-1 alpha-2, alpha-3
 * or numeric code (case-insensitive). For example, {@code JP},
 * {@code JPN} and {@code 392}.
 * </p>
 *
 * <p>
 * The response is JSON that contains the following.
 * </p>
 *
 * <blockquote>
 * <ol>
 *   <li>Country name
 *   <li>ISO 3166-1 alpha-2 code
 *   <li>ISO 3166-1 alpha-3 code
 *   <li>ISO 3166-1 numeric code
 *   <li>Currency
 * </ol>
 * </blockquote>
 *
 * <p>
 * Below is an example response from this API.
 * </p>
 *
 * <blockquote>
 * <pre>
 * {
 *   "name": "Japan",
 *   "alpha2": "JP",
 *   "alpha3": "JPN",
 *   "numeric": 392,
 *   "currency": "JPY"
 * }
 * </pre>
 * </blockquote>
 *
 * @author Takahiko Kawasaki
 */
@Path("/api/country/{code}")
public class CountryEndpoint extends BaseResourceEndpoint
{
    /**
     * JSON generator.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    @GET
    public Response get(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @QueryParam("access_token") String accessToken,
            @PathParam("code") String code,
            @Context HttpServletRequest request)
    {
        // Extract an access token from either the Authorization header or
        // the request parameters. The Authorization header takes precedence.
        // See RFC 6750 (Bearer Token Usage) about the standard ways to accept
        // an access token from a client application.
        String token = extractAccessToken(authorization, accessToken);

        String clientCertificate = extractClientCertificate(request);

        return process(token, code, clientCertificate);
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @FormParam("access_token") String accessToken,
            @PathParam("code") String code,
            @Context HttpServletRequest request)
    {
        // Extract an access token from either the Authorization header or
        // the request parameters. The Authorization header takes precedence.
        // See RFC 6750 (Bearer Token Usage) about the standard ways to accept
        // an access token from a client application.
        String token = extractAccessToken(authorization, accessToken);

        String clientCertificate = extractClientCertificate(request);
        
        return process(token, code, clientCertificate);
    }


    private Response process(String accessToken, String code, String clientCertificate)
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
        validateAccessToken(AuthleteApiFactory.getDefaultApi(), accessToken, null, null, clientCertificate);

        // The access token presented by the client application is valid.

        // Return the requested resource.
        return getResource(code);
    }


    private Response getResource(String code)
    {
        // Look up a CountryCode instance that has the ISO 3166-1 code.
        CountryCode cc = lookup(code);

        Map<String, Object> data = new LinkedHashMap<String, Object>();

        if (cc != null)
        {
            // Pack the data into a Map.
            data.put("name",     cc.getName());
            data.put("alpha2",   cc.getAlpha2());
            data.put("alpha3",   cc.getAlpha3());
            data.put("numeric",  cc.getNumeric());
            data.put("currency", cc.getCurrency());
        }

        // Convert the data to JSON.
        String json = GSON.toJson(data);

        // Create a response of "200 OK".
        return Response.ok(json, "application/json;charset=UTF-8").build();
    }


    /**
     * Look up a {@link CountryCode} instance from an ISO 3166-1 code.
     *
     * @param code
     *         ISO 3166-1 code (alpha-2, alpha-3, or numeric).
     *
     * @return
     *         A {@link CountryCode} instance that corresponds to the
     *         given code. If the given code is not valid, {@code null}
     *         is returned.
     */
    private CountryCode lookup(String code)
    {
        if (code == null)
        {
            // Not found.
            return null;
        }

        // Interpret the code as an ISO 3166-1 alpha-2 or alpha-3 code.
        CountryCode cc = CountryCode.getByCodeIgnoreCase(code);

        if (cc != null)
        {
            // Found.
            return cc;
        }

        try
        {
            // Interpret the code as an ISO 3166-1 numeric code.
            return CountryCode.getByCode(Integer.parseInt(code));
        }
        catch (NumberFormatException e)
        {
            // Not found.
            return null;
        }
    }
}
