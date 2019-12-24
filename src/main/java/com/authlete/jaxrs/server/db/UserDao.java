/*
 * Copyright (C) 2016-2019 Authlete, Inc.
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
package com.authlete.jaxrs.server.db;


import com.authlete.common.dto.Address;
import com.authlete.common.types.User;


/**
 * Operations to access the user database.
 *
 * @author Takahiko Kawasaki
 */
public class UserDao
{
    /**
     * Dummy user database.
     */
    private static final UserEntity[] sUserDB = {
            new UserEntity("1001", "John Smith", "john@example.com",
                    new Address().setCountry("USA"), "+1 (425) 555-1212"),
            new UserEntity("1002", "Jane Smith", "jane@example.com",
                    new Address().setCountry("Chile"), "+56 (2) 687 2400"),
            new UserEntity("1003", "Max Meier", "max@example.com",
                    new Address().setCountry("Germany"), "+49 (30) 210 94-0"),
    };


    /**
     * Get a user entity by a subject.
     *
     * @param subject
     *         A subject (= unique identifier) of a user.
     *
     * @return
     *         A user entity that has the subject. {@code null} is
     *         returned if there is no user who has the subject.
     */
    public static User getBySubject(String subject)
    {
        // For each user.
        for (UserEntity ue : sUserDB)
        {
            if (ue.getSubject().equals(subject))
            {
                // Found the user whose subject matches the specified one.
                return ue;
            }
        }

        // Not found any user whose subject matches the specified one.
        return null;
    }
}
