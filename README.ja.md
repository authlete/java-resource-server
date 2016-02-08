リソースサーバー実装
====================

概要
----

これはリソースサーバーの Java による実装です。 [OpenID Connect Core 1.0][2]
で定義されている[ユーザー情報エンドポイント][1] をサポートし、また、[RFC 6750][3]
(The OAuth 2.0 Authorization Framework: Bearer Token Usage)
に定義されている方法でアクセストークンを受け取る保護リソースエンドポイントの例も含んでいます。

この実装は JAX-RS 2.0 API と [authlete-java-jaxrs][4] ライブラリを用いて書かれています。
JAX-RS は _The Java API for RESTful Web Services_ です。 JAX-RS 2.0 API は
[JSR 339][5] で標準化され、Java EE 7 に含まれています。 一方、authlete-java-jaxrs
は、認可サーバーとリソースサーバーを実装するためのユーティリティークラス群を提供するオープンソースライブラリです。
authlete-java-jaxrs は [authlete-java-common][6] ライブラリを使用しており、こちらは
[Authlete Web API][7] とやりとりするためのオープンソースライブラリです。

クライアントアプリケーションが提示したアクセストークンの有効性を調べるため、
このリソースサーバーは Authlete サーバーに問い合わせをおこないます。
これはつまり、このリソースサーバーは、アクセストークンを発行した認可サーバーが
Authlete をバックエンドサービスとして使用していることを期待していることを意味します。
[java-oauth-server][8] はそのような認可サーバーの実装であり、[OAuth 2.0][9] と
[OpenID Connect][10] をサポートしています。


ライセンス
----------

  Apache License, Version 2.0


ソースコード
------------

  <code>https://github.com/authlete/java-resource-server</code>


Authlete について
-----------------

[Authlete][11] (オースリート) は、OAuth 2.0 & OpenID Connect
の実装をクラウドで提供するサービスです ([overview][12])。 Authlete
が提供するデフォルト実装を使うことにより、もしくは [java-oauth-server][8]
でおこなっているように [Authlete Web API][7]
を用いて認可サーバーを自分で実装することにより、OAuth 2.0 と OpenID Connect
の機能を簡単に実現できます。

この認可サーバーの実装を使うには、Authlete から API
クレデンシャルズを取得し、`authlete.properties` に設定する必要があります。
API クレデンシャルズを取得する手順はとても簡単です。
単にアカウントを登録するだけで済みます ([サインアップ][13])。
詳細は [Getting Started][14] を参照してください。


実行方法
--------

1. このリソースサーバーの実装をダウンロードします。

        $ git clone https://github.com/authlete/java-resource-server.git
        $ cd java-resource-server

2. 設定ファイルを編集して API クレデンシャルズをセットします。

        $ vi authlete.properties

3. [http://localhost:8081/][15] でリソースサーバーを起動します。

        $ mvn jetty:run &

`java-resource-server` は `authlete.properties` を設定ファイルとして参照します。
他のファイルを使用したい場合は、次のようにそのファイルの名前をシステムプロパティー
`authlete.configuration.file` で指定してください。

    $ mvn -Dauthlete.configuration.file=local.authlete.properties jetty:run &


エンドポイント
--------------

この実装は、下表に示すエンドポイントを公開します。

| エンドポイント             | パス                      |
|:---------------------------|:--------------------------|
| ユーザー情報エンドポイント | `/api/userinfo`           |
| カントリーエンドポイント   | `/api/country/{国コード}` |


#### ユーザー情報エンドポイント

ユーザー情報エンドポイントは、[OpenID Connect Core 1.0][2] の
[5.3. UserInfo Endpoint][1] に記述されている要求事項を実装したものです。

このエンドポイントは、アクセストークンを Bearer Token として受け取ります。
つまり、`Authorization: Bearer {アクセストークン}`
を介して、もしくはリクエストパラメーター `access_token={アクセストークン}`
によりアクセストークンを受け取ります。 詳細は [RFC 6750][20] を参照してください。

このエンドポイントは、クライアントアプリケーションの設定に応じて、ユーザー情報を
JSON 形式もしくは [JWT][18] 形式で返します。 クライアントアプリケーションのメタデータの
`userinfo_signed_response_alg` と `userinfo_encrypted_response_alg`
の両方とも指定されていなければ、ユーザー情報は素の JSON で返されます。
そうでない場合は、シリアライズされた JWT で返されます。 Authlete
はクライアントアプリケーションのメタデータを管理するための Web コンソール
([Developer Console][19]) を提供しています。
クライアントアプリケーションのメタデータについては、
[OpenID Connect Dynamic Client Registration 1.0][22] の [2. Client Metadata][21]
を参照してください。

エンドポイントから返されるユーザー情報には、ユーザーの[クレーム][27] が含まれています。
手短に言うと、_クレーム_とは、名前やメールアドレスなどの、ユーザーに関する情報です。
Authlete は (OpenID Connect をサポートしているにもかかわらず)
ユーザーデータを管理しないので、あなたがクレーム値を提供しなければなりません。
これは、`UserInfoRequestHandlerSpi` インターフェースを実装することでおこないます。

このリソースサーバーの実装では、`UserInfoRequestHandlerSpiImpl` が `UserInfoRequestHandlerSpi`
インターフェースの実装で、ダミーデータベースからクレーム値を取り出しています。
実際のユーザーデータベースを参照するよう、この実装を変更する必要があります。


#### カントリーエンドポイント

このリソースサーバーに実装されているカントリーエンドポイントは、
保護リソースエンドポイントの一例に過ぎません。
主な目的は、保護リソースエンドポイントにおけるアクセストークンの有効性の確認方法を示すことです。
具体的には、`BaseResourceEndpoint` クラスの `extractAccessToken` メソッドと
`validateAccessToken` メソッドの使い方を示すことです。

カントリーエンドポイントのパスは `/api/country/{国コード}` で、`{国コード}` の部分は
[ISO 3166-1 コード][23]です ([alpha-2][24]、[alpha-3][25] または [numeric][26])。
例えば、`JP`、`JPN`、`392` は有効は ISO 3166-1 コードで、これらは全て日本を表します。
ですので、次の URL はカントリーエンドポイントに対する有効なリクエストです。

    http://localhost:8081/api/country/JP?access_token={access-token}

エンドポイントからの応答は JSON で、`{国コード}` で指定される国に関する次の情報を含んでいます。

  1. 国名
  2. ISO 3166-1 alpha-2 コード
  3. ISO 3166-1 alpha-3 コード
  4. ISO 3166-1 numeric コード
  5. Currency

次に示すのは応答例です。

```javascript
{
  "name": "Japan",
  "alpha2": "JP",
  "alpha3": "JPN",
  "numeric": 392,
  "currency": "JPY"
}
```

Web API を OAuth のアクセストークンで保護する方法に関する一般的な情報および
Authlete 固有の情報については、[Authlete Definitive Guide][17] の
[Protected Resource][16] を参照してください。


カスタマイズ
------------

新しい保護リソースエンドポイントを追加する最も簡単な方法は、`CountryEndpoint`
がおこなっているように、`BaseResourceEndpoint` のサブクラスを作成する方法です。
しかし、もちろん、直接 `AcessTokenValidator` ([authlete-java-jaxrs][4]) を使用したり
`AuthleteApi.introspection(IntrospectionRequest)` API ([authlete-java-common][6])
をコールしてもかまいません。

新しい保護リソースエンドポイントを追加するに従い、新しいスコープを追加したいと思うでしょう。
あなたの Web API 用に新しいスコープを追加するには、[Service Owner Console][28]
を使用してください。


その他の情報
------------

- [Authlete][11] - Authlete ホームページ
- [authlete-java-common][6] - Java 用 Authlete 共通ライブラリ
- [authlete-java-jaxrs][4] - JAX-RS (Java) 用 Authlete ライブラリ
- [java-oauth-server][8] - 認可サーバーの実装


サポート
--------

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
