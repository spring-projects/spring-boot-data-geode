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

package example.geode.query.cq.event;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.geode.cache.query.CqEvent;
import org.springframework.data.gemfire.listener.annotation.ContinuousQuery;

/**
 * The TemperatureReadingsContinuousQueriesHandler class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public final class TemperatureReadingsContinuousQueriesHandler {

	private final AtomicInteger temperatureReadingsCounter = new AtomicInteger(0);

	private final List<TemperatureReading> boilingTemperatureReadings = new CopyOnWriteArrayList<>();
	private final List<TemperatureReading> freezingTemperatureReadings = new CopyOnWriteArrayList<>();

	public List<TemperatureReading> getBoilingTemperatureReadings() {
		return Collections.unmodifiableList(this.boilingTemperatureReadings);
	}

	public List<Integer> getBoilingTemperatures() {

		return getBoilingTemperatureReadings().stream()
			.map(TemperatureReading::getTemperature)
			.collect(Collectors.toList());
	}

	public List<TemperatureReading> getFreezingTemperatureReadings() {
		return Collections.unmodifiableList(this.freezingTemperatureReadings);
	}

	public List<Integer> getFreezingTemperatures() {

		return getFreezingTemperatureReadings().stream()
			.map(TemperatureReading::getTemperature)
			.collect(Collectors.toList());
	}

	public int getTemperatureReadingCount() {
		return this.temperatureReadingsCounter.get();
	}

	@ContinuousQuery(name = "BoilingTemperatures",
		query = "SELECT * FROM /TemperatureReadings r WHERE r.temperature >= 212")
	public void boilingTemperatures(CqEvent event) {

		TemperatureReading temperatureReading = (TemperatureReading) event.getNewValue();

		this.boilingTemperatureReadings.add(temperatureReading);
		this.temperatureReadingsCounter.incrementAndGet();
	}

	@ContinuousQuery(name = "FreezingTemperatures",
		query = "SELECT * FROM /TemperatureReadings r WHERE r.temperature <= 32")
	public void freezingTemperatures(CqEvent event) {

		TemperatureReading temperatureReading = (TemperatureReading) event.getNewValue();

		this.freezingTemperatureReadings.add(temperatureReading);
		this.temperatureReadingsCounter.incrementAndGet();
	}
}
