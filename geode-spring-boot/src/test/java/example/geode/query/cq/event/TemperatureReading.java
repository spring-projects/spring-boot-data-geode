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

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The {@link TemperatureReading} class is an Abstract Data Type (ADT) modeling a temperature event,
 * tracking the recorded temperature, unit and timestamp of the event.
 *
 * @author John Blum
 * @see example.geode.query.cq.event.TemperatureUnit
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(of = { "temperature", "temperatureUnit" })
@RequiredArgsConstructor(staticName = "of")
public class TemperatureReading {

	@NonNull
	private Integer temperature;

	private LocalDateTime timestamp = LocalDateTime.now();

	private TemperatureUnit temperatureUnit = TemperatureUnit.defaultTemperatureUnit();

	public TemperatureReading at(LocalDateTime timestamp) {
		setTimestamp(timestamp);
		return this;
	}

	public TemperatureReading in(TemperatureUnit temperatureUnit) {
		setTemperatureUnit(temperatureUnit);
		return this;
	}

	@Override
	public String toString() {
		return String.format("%d %s", getTemperature(), getTemperatureUnit());
	}
}
