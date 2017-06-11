# Spring Boot Data Geode

This project adds _Spring Boot_ **auto-configuration** support for both [Apache Geode](http://geode.apache.org/)
and [Pivotal GemFire](https://pivotal.io/pivotal-gemfire).

Among other things, this project builds on [_Spring Boot_](http://projects.spring.io/spring-boot/) as well as [_Spring Data GemFire/Geode_](http://projects.spring.io/spring-data-gemfire/) and additionally offers...

1. _Auto-configures_ a Pivotal GemFire_ or _Apache Geode [ClientCache](http://geode.apache.org/releases/latest/javadoc/org/apache/geode/cache/client/ClientCache.html) instance automatically when either _Spring Data GemFire_ or _Spring Data Geode_ is on the CLASSPATH.

2. _Auto-configures_ either _Pivotal GemFire_ or _Apache Geode_ as a _caching provider_ in [_Spring's Cache Abstraction_](http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#cache) when either _Spring Data GemFire_ or _Spring Data Geode_ are on the CLASSPATH, and your _Spring_ application, `@Configuration` class is annotated with _Spring_ `@EnableCaching` annotation.

3. _Auto-configures_ _Spring Data GemFire_ or _Spring Data Geode_ [Repositories](http://docs.spring.io/spring-data-gemfire/docs/current/reference/html/#gemfire-repositories) when _Spring Data GemFire_ or _Spring Data Geode_ is on the CLASSPATH and _Spring Boot_ detects SDG _Repositories_ in your _Spring Boot_ application.

4. Provides additional support for _Spring Boot_/_Spring Data GemFire_/_Spring Data Geode_ applications deployed to PCF using either the PCC (_Pivotal Cloud Caching_) or SSC (_Session State Caching_) services.  Also, when using SSC, you can also take advantage of [_Spring Session Data GemFire_](https://github.com/spring-projects/spring-session-data-geode).

This, along with many other things will be provided in and by this project.
