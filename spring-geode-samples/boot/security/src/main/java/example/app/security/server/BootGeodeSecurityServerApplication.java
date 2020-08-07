/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package example.app.security.server;

import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.internal.security.shiro.GeodePermissionResolver;

import org.apache.shiro.realm.text.PropertiesRealm;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableSecurity;

/**
 * A Spring Boot, Apache Geode {@literal peer} {@link CacheServer} application serving cache clients.
 *
 * This Apache Geode {@link CacheServer} and {@literal peer member} configures Apache Geode Security
 * using Apache Shiro.
 *
 * @author Patrick Johnson
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.apache.shiro.realm.text.PropertiesRealm
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableSecurity
 * @see <a href="https://shiro.apache.org/">Apache Shiro</a>
 * @since 1.4.0
 */
// tag::class[]
@SpringBootApplication
@CacheServerApplication
@EnableSecurity
public class BootGeodeSecurityServerApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(BootGeodeSecurityServerApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	// tag::realm[]
	@Bean
	PropertiesRealm shiroRealm() {

		PropertiesRealm propertiesRealm = new PropertiesRealm();

		propertiesRealm.setResourcePath("classpath:shiro.properties");
		propertiesRealm.setPermissionResolver(new GeodePermissionResolver());

		return propertiesRealm;
	}
	// end::realm[]
}
// end::class[]
