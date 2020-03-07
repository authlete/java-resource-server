/*
 * Copyright (C) 2016-2020 Authlete, Inc.
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


import java.util.List;
import com.authlete.common.assurance.VerifiedClaims;
import com.authlete.common.assurance.constraint.VerifiedClaimsConstraint;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.db.UserDao;
import com.authlete.jaxrs.server.db.VerifiedClaimsDao;
import com.authlete.jaxrs.spi.UserInfoRequestHandlerSpiAdapter;


/**
 * Implementation of {@link com.authlete.jaxrs.spi.UserInfoRequestHandlerSpi
 * UserInfoRequestHandlerSpi} interface which needs to be given to the
 * constructor of {@link com.authlete.jaxrs.UserInfoRequestHandler
 * UserInfoRequestHandler}.
 *
 * @author Takahiko Kawasaki
 */
public class UserInfoRequestHandlerSpiImpl extends UserInfoRequestHandlerSpiAdapter
{
    private User mUser;


    @Override
    public void prepareUserClaims(String subject, String[] claimNames)
    {
        // Look up a user who has the subject.
        mUser = UserDao.getBySubject(subject);
    }


    @Override
    public Object getUserClaim(String claimName, String languageTag)
    {
        // If looking up a user has failed in prepareUserClaims().
        if (mUser == null)
        {
            // No claim is available.
            return null;
        }

        // Get the value of the claim.
        return mUser.getClaim(claimName, languageTag);
    }


    @Override
    public List<VerifiedClaims> getVerifiedClaims(String subject, VerifiedClaimsConstraint constraint)
    {
        return VerifiedClaimsDao.get(subject, constraint);
    }
}
