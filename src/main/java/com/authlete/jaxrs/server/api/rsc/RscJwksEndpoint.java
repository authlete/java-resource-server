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
package com.authlete.jaxrs.server.api.rsc;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.nimbusds.jose.util.IOUtils;


/**
 * An implementation of an endpoint that advertises a JSON Web Key Set document
 * of the protected resource.
 *
 * @see <a href="https://datatracker.ietf.org/doc/draft-ietf-oauth-resource-metadata/"
 *      >OAuth 2.0 Protected Resource Metadata</a>
 */
@Path("/api/rsc/jwks")
public class RscJwksEndpoint
{
    private static final String JWKSET_FILE = "/resource.jwkset.json";
    private static String jwkset;


    @GET
    public Response get()
    {
        if (jwkset == null)
        {
            // Load the JWK Set document.
            jwkset = loadJwkset();
        }

        // Create a response with the status code "200 OK".
        return Response.ok(jwkset, "application/jwk-set+json").build();
    }


    private String loadJwkset()
    {
        try
        {
            // Read the content of the JWK Set document.
            return readAsString(JWKSET_FILE);
        }
        catch (IOException cause)
        {
            // Write an error log.
            System.err.format("Failed to load the JWK Set documnet: %s", cause.getMessage());
            cause.printStackTrace();

            // JSON including a simple error message.
            // Malformed JWK Set, as the "keys" property is missing.
            return "{\n  \"error\": \"The JWK Set document could not be loaded.\"\n}\n";
        }
    }


    private String readAsString(String file) throws IOException
    {
        // Retrieve the content of the specified resource file as a string.
        try (InputStream is = getClass().getResourceAsStream(file))
        {
            return IOUtils.readInputStreamToString(is, StandardCharsets.UTF_8);
        }
    }
}
