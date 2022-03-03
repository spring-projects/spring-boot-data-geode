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

import java.io.PrintStream;
import java.util.PrimitiveIterator;
import java.util.Random;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import example.app.temp.model.TemperatureReading;
import example.app.temp.repo.TemperatureReadingRepository;

// tag::class[]
@Service
@SuppressWarnings("unused")
public class TemperatureSensor {

	private final PrimitiveIterator.OfInt temperatureStream =
		new Random(System.currentTimeMillis())
			.ints(-100, 400)
			.iterator();

	private final TemperatureReadingRepository repository;

	public TemperatureSensor(TemperatureReadingRepository repository) {

		Assert.notNull(repository, "TemperatureReadingRepository is required");

		this.repository = repository;
	}

	@Scheduled(fixedRateString = "${example.app.temp.sensor.reading.schedule.rate:1000}")
	public void readTemperature() {

		TemperatureReading temperatureReading =
			TemperatureReading.newTemperatureReading(temperatureStream.nextInt());

		this.repository.save(log(temperatureReading));
	}

	private TemperatureReading log(TemperatureReading temperatureReading) {

		PrintStream out = temperatureReading.isNormal() ? System.out : System.err;

		out.printf("TEMPERATURE READING [%s]%n", temperatureReading);

		return temperatureReading;
	}
}
// end::class[]
