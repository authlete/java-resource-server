/*
 * Copyright (C) 2024 Authlete, Inc.
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
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.authlete.jaxrs.util.RequestUrlResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * An implementation of protected resource metadata.
 *
 * @see <a href="https://datatracker.ietf.org/doc/draft-ietf-oauth-resource-metadata/"
 *      >OAuth 2.0 Protected Resource Metadata</a>
 */
@Path("/.well-known/oauth-protected-resource")
public class ResourceMetadataEndpoint
{
    // JSON processor
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    @GET
    public Response get(@Context HttpServletRequest request)
    {
        // Metadata
        Map<String, Object> metadata = buildMetadata(request);

        // Convert the metadata to JSON.
        String json = GSON.toJson(metadata);
        json += "\n";

        // Create a response with the status code "200 OK".
        return Response.ok(json, MediaType.APPLICATION_JSON_TYPE).build();
    }


    private static Map<String, Object> buildMetadata(HttpServletRequest request)
    {
        // The original request URL.
        URI uri = resolveOriginalRequestUrl(request);

        // "resource": The protected resource's Resource Identifier.
        String resource = String.format("%s://%s", uri.getScheme(), uri.getAuthority());

        // "jwks_uri": The URL of the protected resource's JSON Web Key Set document.
        String jwksUri = String.format("%s/api/rsc/jwks", resource);

        // Metadata
        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put("resource", resource);
        metadata.put("jwks_uri", jwksUri);

        return metadata;
    }


    private static URI resolveOriginalRequestUrl(HttpServletRequest request)
    {
        // The original request URL.
        return URI.create(new RequestUrlResolver().resolve(request));
    }
}
