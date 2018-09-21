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

package example.app.temp.repo;

import java.util.List;

import org.springframework.data.gemfire.repository.Query;
import org.springframework.data.repository.CrudRepository;

import example.app.temp.model.TemperatureReading;

@SuppressWarnings("unused")
public interface TemperatureReadingRepository extends CrudRepository<TemperatureReading, Long> {

	List<TemperatureReading> findByTimestampGreaterThanAndTimestampLessThan(Long timestampLowerBound,
		Long timestampUpperBound);

	@Query("SELECT count(*) FROM /TemperatureReadings WHERE temperature >= 212")
	Integer countBoilingTemperatureReadings();

	@Query("SELECT count(*) FROM /TemperatureReadings WHERE temperature <= 32")
	Integer countFreezingTemperatureReadings();

}
