/*
 * Copyright (C) 2018 Authlete, Inc.
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


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.util.Utils;
import com.authlete.jaxrs.BaseResourceEndpoint;


/**
 * This is a dummy implementation of {@code /account-requests} API
 * which is defined in <i>"Account and Transaction API Specification"</i>
 * of UK Open Banking.
 */
@Path("/api/open-banking/v1.1/account-requests")
public class AccountRequestsEndpoint extends BaseResourceEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post()
    {
        // {
        //   "Data" : {
        //     "AccountRequestId" : "<string>"
        //   }
        // }

        Map<String, Object> root = new HashMap<String, Object>();

        // Data
        Map<String, Object> data = new HashMap<String, Object>();
        root.put("Data", data);

        // Data.AccountRequestId
        data.put("AccountRequestId", UUID.randomUUID().toString());

        // 201 Created, application/json
        return Response
                .status(Status.CREATED)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Utils.toJson(root, true))
                .build();
    }
}
