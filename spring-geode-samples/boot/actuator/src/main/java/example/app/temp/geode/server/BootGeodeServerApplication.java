/*
 * Copyright 2018 the original author or authors.
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

package example.app.temp.geode.server;

import java.util.Optional;

import org.apache.geode.cache.GemFireCache;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.gemfire.IndexFactoryBean;
import org.springframework.data.gemfire.RegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableStatistics;
import org.springframework.data.gemfire.config.annotation.RegionConfigurer;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.geode.config.annotation.UseGroups;
import org.springframework.scheduling.annotation.EnableScheduling;

import example.app.temp.model.TemperatureReading;
import example.app.temp.repo.TemperatureReadingRepository;
import example.app.temp.service.TemperatureSensor;

@SpringBootApplication
@CacheServerApplication(name = "TemperatureServiceServer")
@EnableEntityDefinedRegions(basePackageClasses = TemperatureReading.class)
@EnableGemfireRepositories(basePackageClasses = TemperatureReadingRepository.class)
@EnableScheduling
@EnableStatistics
@UseGroups("TemperatureSensors")
@SuppressWarnings("unused")
public class BootGeodeServerApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(BootGeodeServerApplication.class)
			.web(WebApplicationType.SERVLET)
			.build()
			.run(args);
	}

	@Bean
	TemperatureSensor temperatureSensor(TemperatureReadingRepository repository) {
		return new TemperatureSensor(repository);
	}

	@Bean
	RegionConfigurer temperatureReadingsConfigurer() {

		return new RegionConfigurer() {

			@Override
			public void configure(String beanName, RegionFactoryBean<?, ?> regionBean) {

				Optional.ofNullable(beanName)
					.filter("TemperatureReadings"::equals)
					.ifPresent(it -> regionBean.setStatisticsEnabled(true));
			}
		};
	}

	@Bean
	@DependsOn("TemperatureReadings")
	IndexFactoryBean temperatureReadingTemperatureIndex(GemFireCache gemfireCache) {

		IndexFactoryBean temperatureTimestampIndex = new IndexFactoryBean();

		temperatureTimestampIndex.setCache(gemfireCache);
		temperatureTimestampIndex.setExpression("temperature");
		temperatureTimestampIndex.setFrom("/TemperatureReadings");
		temperatureTimestampIndex.setName("TemperatureReadingTemperatureIdx");

		return temperatureTimestampIndex;
	}

	@Bean
	@DependsOn("TemperatureReadings")
	IndexFactoryBean temperatureReadingTimestampIndex(GemFireCache gemfireCache) {

		IndexFactoryBean temperatureTimestampIndex = new IndexFactoryBean();

		temperatureTimestampIndex.setCache(gemfireCache);
		temperatureTimestampIndex.setExpression("timestamp");
		temperatureTimestampIndex.setFrom("/TemperatureReadings");
		temperatureTimestampIndex.setName("TemperatureReadingTimestampIdx");

		return temperatureTimestampIndex;
	}
}
