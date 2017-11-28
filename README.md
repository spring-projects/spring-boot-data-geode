# Spring Boot for Apache Geode

_Spring Boot for Apache Geode_ extends [_Spring Boot_](http://projects.spring.io/spring-boot/) with _auto-configuration_ support as well as other _convention or configuration_ features to simplify the development of _Spring_ applications using either [Pivotal GemFire](https://pivotal.io/pivotal-gemfire) or [Apache Geode](http://geode.apache.org/) in a _Spring_ context.

This project builds on both [_Spring Data GemFire_](http://projects.spring.io/spring-data-gemfire/) and [Spring Data Geode](https://github.com/spring-projects/spring-data-geode).

### Project Goals

This project adds _Spring Boot_ **auto-configuration** support for both [Apache Geode](http://geode.apache.org/)
and [Pivotal GemFire](https://pivotal.io/pivotal-gemfire).

Among other things, this project builds on [_Spring Boot_](http://projects.spring.io/spring-boot/) as well as [_Spring Data GemFire/Geode_](http://projects.spring.io/spring-data-gemfire/) and additionally offers...

1. _Auto-configures_ a Pivotal GemFire_ or _Apache Geode_ [ClientCache](http://geode.apache.org/releases/latest/javadoc/org/apache/geode/cache/client/ClientCache.html) instance automatically when either _Spring Data GemFire_ or _Spring Data Geode_ is on the application's CLASSPATH.

2. _Auto-configures_ either _Pivotal GemFire_ or _Apache Geode_ as a _caching provider_ in [_Spring's Cache Abstraction_](http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#cache) when either _Spring Data GemFire_ or _Spring Data Geode_ are on the application's CLASSPATH.

3. _Auto-configures_ _Spring Data GemFire_ or _Spring Data Geode_ [Repositories](http://docs.spring.io/spring-data-gemfire/docs/current/reference/html/#gemfire-repositories) when _Spring Data GemFire_ or _Spring Data Geode_ is on the application's CLASSPATH and _Spring Boot_ detects SDG _Repositories_ in your _Spring Boot_ application.

4. Provides additional support for _Spring Boot_/_Spring Data GemFire_/_Spring Data Geode_ applications deployed to PCF using either the PCC (_Pivotal Cloud Caching_) or SSC (_Session State Caching_) services.  Also, when using SSC, you can also take advantage of [_Spring Session Data GemFire_](https://github.com/spring-projects/spring-session-data-geode).

5. As an added benefit, _Spring Boot Data GemFire_ will automatically authenticate your _Spring Boot_, _Pivotal GemFire_-based application when deployed to PCF and the application is granted access to and connects with a secure PCC instance for all of its caching concerns.

This, along with many other things will be provided in and by this project.
