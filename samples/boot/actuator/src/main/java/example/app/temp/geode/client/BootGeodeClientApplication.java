/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app.temp.geode.client;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.geode.config.annotation.UseGroups;
import org.springframework.geode.config.annotation.UseMemberName;

import example.app.temp.event.BoilingTemperatureEvent;
import example.app.temp.event.FreezingTemperatureEvent;
import example.app.temp.event.TemperatureEvent;
import example.app.temp.model.TemperatureReading;
import example.app.temp.service.TemperatureMonitor;

@SpringBootApplication
@EnableEntityDefinedRegions(basePackageClasses = TemperatureReading.class)
@UseGroups("TemperatureMonitors")
@UseMemberName("TemperatureMonitoringService")
@SuppressWarnings("unused")
public class BootGeodeClientApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(BootGeodeClientApplication.class)
			.web(WebApplicationType.SERVLET)
			.build()
			.run(args);
	}

	@Bean
	TemperatureMonitor temperatureMonitor(ApplicationEventPublisher applicationEventPublisher) {
		return new TemperatureMonitor(applicationEventPublisher);
	}

	@EventListener(classes = { BoilingTemperatureEvent.class, FreezingTemperatureEvent.class })
	public void temperatureEventHandler(TemperatureEvent temperatureEvent) {

		System.err.printf("%1$s TEMPERATURE READING [%2$s]%n",
			temperatureEvent instanceof BoilingTemperatureEvent ? "HOT" : "COLD",
				temperatureEvent.getTemperatureReading());
	}
}
