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


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.util.IOUtils;


/**
 * The private key used for signing HTTP responses in accordance with
 * FAPI 2&#x2E;0 Message Signing.
 *
 * @see <a href="https://openid.bitbucket.io/fapi/fapi-2_0-message-signing.html"
 *      >FAPI 2.0 Message Signing</a>
 */
class ResponseSigningKey
{
    private static final String KEY_FILE = "/response-signing.jwk";
    private static JWK key;


    public static JWK get()
    {
        if (key == null)
        {
            // Load the key.
            key = loadKey();
        }

        return key;
    }


    private static JWK loadKey()
    {
        try
        {
            // Read the content of the key file.
            String content = readAsString(KEY_FILE);

            // Parse the content as a JWK.
            return JWK.parse(content);
        }
        catch (IOException cause)
        {
            // Write an error log.
            System.err.format("Failed to load the response signing key: %s%n", cause.getMessage());
            cause.printStackTrace();

            return null;
        }
        catch (ParseException cause)
        {
            // Write an error log.
            System.err.format("Failed to parse the signing key: %s%n", cause.getMessage());
            cause.printStackTrace();

            return null;
        }
    }


    private static String readAsString(String file) throws IOException
    {
        // Retrieve the content of the specified resource file as a string.
        try (InputStream is = ResponseSigningKey.class.getResourceAsStream(file))
        {
            return IOUtils.readInputStreamToString(is, StandardCharsets.UTF_8);
        }
    }
}
