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
package example.app.petclinic.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Abstract Data Type (ADT) modeling a pet.
 *
 * @author John Blum
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.gemfire.mapping.annotation.Region
 * @since 1.2.1
 */
@Region("Pets")
@ToString(of = "name")
@EqualsAndHashCode(of = "name")
@RequiredArgsConstructor(staticName = "newPet")
@SuppressWarnings("unused")
public class Pet {

	@Getter
	private LocalDateTime vaccinationDateTime;

	@Id @NonNull @Getter
	private String name;

	@Getter
	private Type petType;

	public Pet as(Type petType) {
		this.petType = petType;
		return this;
	}

	public void vaccinate() {
		this.vaccinationDateTime = LocalDateTime.now();
	}

	public enum Type {
		CAT,
		DOG,
		RABBIT,
	}
}
