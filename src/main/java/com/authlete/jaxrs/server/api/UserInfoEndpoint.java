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


import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseUserInfoEndpoint;


/**
 * An implementation of userinfo endpoint (<a href=
 * "http://openid.net/specs/openid-connect-core-1_0.html#UserInfo"
 * >OpenID Connect Core 1&#x2E;0, 5&#x2E;3&#x2E; UserInfo Endpoint</a>).
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#UserInfo"
 *      >OpenID Connect Core 10, 5.3. UserInfo Endpoint</a>
 *
 * @author Takahiko Kawasaki
 */
@Path("/api/userinfo")
public class UserInfoEndpoint extends BaseUserInfoEndpoint
{
    /**
     * The userinfo endpoint for {@code GET} method.
     *
     * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#UserInfoRequest"
     *      >OpenID Connect Core 1.0, 5.3.1. UserInfo Request</a>
     */
    @GET
    public Response get(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @QueryParam("access_token") String accessToken)
    {
        // Handle the userinfo request.
        return handle(extractAccessToken(authorization, accessToken));
    }


    /**
     * The userinfo endpoint for {@code POST} method.
     *
     * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#UserInfoRequest"
     *      >OpenID Connect Core 1.0, 5.3.1. UserInfo Request</a>
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @FormParam("access_token") String accessToken)
    {
        // Handle the userinfo request.
        return handle(extractAccessToken(authorization, accessToken));
    }


    /**
     * Handle the userinfo request.
     */
    private Response handle(String accessToken)
    {
        return handle(AuthleteApiFactory.getDefaultApi(),
                new UserInfoRequestHandlerSpiImpl(), accessToken);
    }
}
