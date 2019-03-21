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

package example.app.temp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.gemfire.mapping.annotation.Region;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Region("TemperatureReadings")
@RequiredArgsConstructor(staticName = "newTemperatureReading")
@SuppressWarnings("unused")
public class TemperatureReading {

	private static final int BOILING_TEMPERATURE = 212;
	private static final int FREEZING_TEMPERATURE = 32;

	@Id
	private Long timestamp = System.currentTimeMillis();

	@NonNull
	private Integer temperature;

	@Transient
	public boolean isBoiling() {

		Integer temperature = getTemperature();

		return temperature != null && temperature >= BOILING_TEMPERATURE;
	}

	@Transient
	public boolean isNormal() {
		return !(isBoiling() || isFreezing());
	}

	@Transient
	public boolean isFreezing() {

		Integer temperature = getTemperature();

		return temperature != null && temperature <= FREEZING_TEMPERATURE;
	}

	@Override
	public String toString() {
		return String.format("%d °F", getTemperature());
	}
}
