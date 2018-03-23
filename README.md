Resource Server Implementation in Java
======================================

Overview
--------

This is a resource server implementation in Java. It supports a
[userinfo endpoint][1] defined in [OpenID Connect Core 1.0][2] and
includes an example of a protected resource endpoint that accepts
an access token in the ways defined in [RFC 6750][3] (The OAuth 2.0
Authorization Framework: Bearer Token Usage).

This implementation is written using JAX-RS 2.0 API and
[authlete-java-jaxrs][4] library. JAX-RS is _The Java API for RESTful
Web Services_. JAX-RS 2.0 API has been standardized by [JSR 339][5]
and it is included in Java EE 7. On the other hand,
authlete-java-jaxrs library is an open source library which provides
utility classes for developers to implement an authorization server
and a resource server. authlete-java-jaxrs in turn uses
[authlete-java-common][6] library which is another open source
library to communicate with [Authlete Web APIs][7].

To validate an access token presented by a client application, this
resource server makes an inquiry to the Authlete server. This means
that this resource server expects that the authorization server which
has issued the access token uses Authlete as a backend service.
[java-oauth-server][8] is such an authorization server implementation
and it supports [OAuth 2.0][9] and [OpenID Connect][10].


License
-------

  Apache License, Version 2.0


Source Code
-----------

  <code>https://github.com/authlete/java-resource-server</code>


About Authlete
--------------

[Authlete][11] is a cloud service that provides an implementation of OAuth 2.0
& OpenID Connect ([overview][12]). You can easily get the functionalities of
OAuth 2.0 and OpenID Connect either by using the default implementation
provided by Authlete or by implementing your own authorization server using
[Authlete Web APIs][7] as [java-oauth-server][8] does.

To use this resource server implementation, you need to get API credentials
from Authlete and set them in `authlete.properties`. The steps to get API
credentials are very easy. All you have to do is just to register your account
([sign up][13]). See [Getting Started][14] for details.


How To Run
----------

1. Download the source code of this resource server implementation.

        $ git clone https://github.com/authlete/java-resource-server.git
        $ cd java-resource-server

2. Edit the configuration file to set the API credentials of yours.

        $ vi authlete.properties

3. Make sure that you have installed [maven][29] and set `JAVA_HOME` properly.

4. Start the resource server on [http://localhost:8081/][15].

        $ mvn jetty:run &

#### Run With Docker

If you would prefer to use Docker, just hit the following command after the step 2.

    $ docker-compose up

#### Configuration File

`java-resource-server` refers to `authlete.properties` as a configuration file.
If you want to use another different file, specify the name of the file by
the system property `authlete.configuration.file` like the following.

    $ mvn -Dauthlete.configuration.file=local.authlete.properties jetty:run &


Endpoints
---------

This implementation exposes endpoints as listed in the table below.

| Endpoint          | Path                          |
|:------------------|:------------------------------|
| UserInfo Endpoint | `/api/userinfo`               |
| Country Endpoint  | `/api/country/{country-code}` |


#### UserInfo Endpoint

The userinfo endpoint is an implementation of the requirements described in
[5.3. UserInfo Endpoint][1] of [OpenID Connect Core 1.0][2].

The endpoint accepts an access token as a Bearer Token. That is, it accepts
an access token via `Authorization: Bearer {access-token}` or by a request
parameter `access_token={access-token}`. See [RFC 6750][20] for details.

The endpoint returns user information in JSON or [JWT][18] format, depending
on the configuration of the client application. If both
`userinfo_signed_response_alg` and `userinfo_encrypted_response_alg` of
the metadata of the client application are not specified, user information
is returned as a plain JSON. Otherwise, it is returned as a serialized JWT.
Authlete provides you with a Web console ([Developer Console][19]) to manage
metadata of client applications. As for metadata of client applications, see
[2. Client Metadata][21] in [OpenID Connect Dynamic Client Registration 1.0][22].

User information returned from the endpoint contains [claims][27] of the user.
In short, _claims_ are pieces of information about the user such as a given
name and an email address. Because Authlete does not manage user data (although
it supports OpenID Connect), you have to provide claim values. It is achieved
by implementing `UserInfoRequestHandlerSpi` interface.

In this resource server implementation, `UserInfoRequestHandlerSpiImpl` is
an example implementation of `UserInfoRequestHandlerSpi` interface and it
retrieves claim values from a dummy database. You need to modify the
implementation to make it refer to your actual user database.


#### Country Endpoint

The country endpoint implemented in this resource server is just an example
of a protected resource endpoint. Its main purpose is to show how to validate
an access token at a protected resource endpoint. To be concrete, the purpose
is to show usage of `extractAccessToken` method and `validateAccessToken`
method defined in `BaseResourceEndpoint` class.

The path of the country endpoint is `/api/country/{country-code}` where
`{country-code}` is an [ISO 3166-1 code][23] ([alpha-2][24], [alpha-3][25],
or [numeric][26]). For example, `JP`, `JPN` and `392` are valid ISO 3166-1
codes and all of them represent Japan. Therefore, the following URL is a
valid request to the country endpoint.

    http://localhost:8081/api/country/JP?access_token={access-token}

The response from the endpoint is JSON that contains the following information
about the country identified by the `{country-code}`.

  1. Country name
  2. ISO 3166-1 alpha-2 code
  3. ISO 3166-1 alpha-3 code
  4. ISO 3166-1 numeric code
  5. Currency

The following is an example response.

```javascript
{
  "name": "Japan",
  "alpha2": "JP",
  "alpha3": "JPN",
  "numeric": 392,
  "currency": "JPY"
}
```

As for generic and Authlete-specific information regarding how to protect
Web APIs by OAuth access tokens, see [Protected Resource][16] in
[Authlete Definitive Guide][17].


Customization
-------------

The simplest way to add a new protected resource endpoint is to create a
subclass of `BaseResourceEndpoint` as `CountryEndpoint` does. However,
of course, it is okay for you to use `AccessTokenValidator` (in
[authlete-java-jaxrs][4]) or call
`AuthleteApi.introspection(IntrospectionRequest)` API (in
[authlete-java-common][6]) directly.

As you add new protected resource endpoints, you will want to add new scopes.
Use [Service Owner Console][28] to add new scopes for your Web APIs.


See Also
--------

- [Authlete][11] - Authlete Home Page
- [authlete-java-common][6] - Authlete Common Library for Java
- [authlete-java-jaxrs][4] - Authlete Library for JAX-RS (Java)
- [java-oauth-server][8] - Authorization Server Implementation


Support
-------

[Authlete, Inc.][11]<br/>
support@authlete.com


[1]: http://openid.net/specs/openid-connect-core-1_0.html#UserInfo
[2]: http://openid.net/specs/openid-connect-core-1_0.html
[3]: http://tools.ietf.org/html/rfc6750
[4]: https://github.com/authlete/authlete-java-jaxrs
[5]: https://jcp.org/en/jsr/detail?id=339
[6]: https://github.com/authlete/authlete-java-common
[7]: https://www.authlete.com/documents/apis
[8]: https://github.com/authlete/java-oauth-server
[9]: http://tools.ietf.org/html/rfc6749
[10]: http://openid.net/connect/
[11]: https://www.authlete.com/
[12]: https://www.authlete.com/documents/overview
[13]: https://so.authlete.com/accounts/signup
[14]: https://www.authlete.com/documents/getting_started
[15]: http://localhost:8081/
[16]: https://www.authlete.com/documents/definitive_guide/protected_resource
[17]: https://www.authlete.com/documents/definitive_guide
[18]: http://tools.ietf.org/html/rfc7519
[19]: https://www.authlete.com/documents/cd_console
[20]: http://tools.ietf.org/html/rfc6750
[21]: http://openid.net/specs/openid-connect-registration-1_0.html#ClientMetadata
[22]: http://openid.net/specs/openid-connect-registration-1_0.html
[23]: http://en.wikipedia.org/wiki/ISO_3166-1
[24]: http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
[25]: http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
[26]: http://en.wikipedia.org/wiki/ISO_3166-1_numeric
[27]: http://openid.net/specs/openid-connect-core-1_0.html#Claims
[28]: https://www.authlete.com/documents/so_console
[29]: https://maven.apache.org/
