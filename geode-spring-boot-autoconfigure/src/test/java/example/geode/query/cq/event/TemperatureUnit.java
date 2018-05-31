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

import java.util.Locale;
import java.util.Optional;

/**
 * The {@link TemperatureUnit} enum is an enumeration of different temperature units
 * as defined by International System of Units (SI).
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public enum TemperatureUnit {

	CELSIUS("°C"),
	FAHRENHEIT("°F"),
	KELVIN("K");

	public static TemperatureUnit defaultTemperatureUnit() {

		return Optional.of(Locale.getDefault())
			.map(Locale::getISO3Country)
			.filter(Locale.US.getISO3Country()::equalsIgnoreCase)
			.map(it -> TemperatureUnit.FAHRENHEIT)
			.orElse(TemperatureUnit.CELSIUS);
	}

	private final String symbol;

	TemperatureUnit(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return this.symbol;
	}

	@Override
	public String toString() {
		return getSymbol();
	}
}
