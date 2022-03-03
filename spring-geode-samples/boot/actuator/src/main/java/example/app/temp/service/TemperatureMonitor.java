/*
 * Copyright 2017-present the original author or authors.
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
package example.app.temp.service;

import java.util.Optional;

import org.apache.geode.cache.query.CqEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.gemfire.listener.annotation.ContinuousQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import example.app.temp.event.BoilingTemperatureEvent;
import example.app.temp.event.FreezingTemperatureEvent;
import example.app.temp.model.TemperatureReading;

// tag::class[]
@Service
@SuppressWarnings("unused")
public class TemperatureMonitor {

	private final ApplicationEventPublisher applicationEventPublisher;

	public TemperatureMonitor(ApplicationEventPublisher applicationEventPublisher) {

		Assert.notNull(applicationEventPublisher, "ApplicationEventPublisher is required");

		this.applicationEventPublisher = applicationEventPublisher;
	}

	@ContinuousQuery(name = "BoilingTemperatureMonitor",
		query = "SELECT * FROM /TemperatureReadings WHERE temperature >= 212")
	public void boilingTemperatureReadings(CqEvent event) {

		Optional.ofNullable(event)
			.map(CqEvent::getNewValue)
			.filter(TemperatureReading.class::isInstance)
			.map(TemperatureReading.class::cast)
			.map(it -> new BoilingTemperatureEvent(this, it))
			.ifPresent(this.applicationEventPublisher::publishEvent);
	}

	@ContinuousQuery(name = "FreezingTemperatureMonitor",
		query = "SELECT * FROM /TemperatureReadings WHERE temperature <= 32")
	public void freezingTemperatureReadings(CqEvent event) {

		Optional.ofNullable(event)
			.map(CqEvent::getNewValue)
			.filter(TemperatureReading.class::isInstance)
			.map(TemperatureReading.class::cast)
			.map(it -> new FreezingTemperatureEvent(this, it))
			.ifPresent(this.applicationEventPublisher::publishEvent);
	}
}
// end::class[]
