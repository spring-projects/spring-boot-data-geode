# Spring Boot Data Geode

This project adds _Spring Boot_ **auto-configuration** support for both http://geode.apache.org/[Apache Geode] 
and https://pivotal.io/pivotal-gemfire[Pivotal GemFire].

Among other things, this project builds on http://projects.spring.io/spring-boot/[_Spring Boot_] as well as http://projects.spring.io/spring-data-gemfire/[_Spring Data GemFire/Geode_] and additionally offers...

1. Auto-configures a Pivotal GemFire/Apache Geode http://geode.apache.org/releases/latest/javadoc/org/apache/geode/cache/client/ClientCache.html[ClientCache] instance automatically when either _Spring Data GemFire_ or _Spring Data Geode_ is on the CLASSPATH.

2. Auto-configures either Pivotal GemFire_ or Apache Geode as a _caching provider_ in [Spring's Cache Abstraction] when SDG^2 is on the CLASSPATH and the _Spring_ `@EnableCaching` annotation is specified on your Spring application, `@Configuration` class.

3. Auto-configures _Spring Data GemFire_ or _Spring Data Geode_ http://docs.spring.io/spring-data-gemfire/docs/current/reference/html/#gemfire-repositories[Repositories] when SDG^2 is on the CLASSPATH and _Spring Boot_ detects SDG _Repositories_ in the _Spring Boot_ application.

4. Provides additional support for _Spring Boot_/_Spring Data GemFire_/_Spring Data Geode_ applications deployed to PCF using either the PCC (_Pivotal Cloud Caching_) or SSC (_Session State Caching_) services.  Also, when using SSC, you can also take advantage of https://github.com/spring-projects/spring-session-data-geode[_Spring Session Data GemFire_].

This, along with many other things will be provided in and by this project.
