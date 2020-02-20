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
package example.app.petclinic;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;

import org.apache.shiro.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctions;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.petclinic.function.PetServiceFunctionExecutions;
import example.app.petclinic.model.Pet;
import example.app.petclinic.repo.PetRepository;

/**
 * Smoke Tests asserting the proper injection and execution of an Apache Geode {@link Function} using Spring Data
 * for Apache Geode {@link Function} annotation support in a Spring (Boot) context.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.execute.Function
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see PetServiceFunctionExecutions
 * @see example.app.petclinic.model.Pet
 * @see example.app.petclinic.repo.PetRepository
 * @since 1.2.1
 */
@ActiveProfiles("petclinic-client-function-execution")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class PetClinicApplicationSmokeTests extends ForkingClientServerIntegrationTestsSupport {

	@BeforeClass
	public static void startGeodeServer() throws Exception {
		startGemFireServer(GeodeServerTestConfiguration.class,
			"-Dspring.profiles.active=petclinic-server-function-execution");
	}

	private Pet castle = Pet.newPet("Castle").as(Pet.Type.CAT);
	private Pet cocoa = Pet.newPet("Cocoa").as(Pet.Type.CAT);
	private Pet maha = Pet.newPet("Maha").as(Pet.Type.DOG);
	private Pet mittens = Pet.newPet("Mittens").as(Pet.Type.CAT);

	private Set<Pet> pets = CollectionUtils.asSet(castle, cocoa, maha, mittens);

	@Autowired
	private PetRepository petRepository;

	@Autowired
	private PetServiceFunctionExecutions petServiceFunctions;

	@Before
	public void setup() {

		assertThat(this.petRepository.count()).isEqualTo(0);

		this.pets.forEach(pet -> assertThat(pet.getVaccinationDateTime()).isNull());
		this.petRepository.saveAll(this.pets);

		assertThat(this.petRepository.count()).isEqualTo(this.pets.size());
	}

	@Test
	public void administerPetVaccinationsIsSuccessful() {

		LocalDateTime beforeVaccinations = LocalDateTime.now();

		this.petServiceFunctions.administerPetVaccinations();

		LocalDateTime afterVaccinations = LocalDateTime.now();

		this.petRepository.findAll().forEach(pet -> {

			assertThat(pet.getVaccinationDateTime())
				.describedAs("Vaccinations [%s] for [%s] was not correct", pet.getVaccinationDateTime(), pet)
				.isAfterOrEqualTo(beforeVaccinations);

			assertThat(pet.getVaccinationDateTime()).isBeforeOrEqualTo(afterVaccinations);
		});
	}

	@Profile("petclinic-client-function-execution")
	@EnableEntityDefinedRegions(basePackageClasses = Pet.class)
	@SpringBootApplication(scanBasePackageClasses = PetClinicApplicationSmokeTests.class)
	static class GeodeClientTestConfiguration { }

	@Profile("petclinic-peer-function-execution")
	@PeerCacheApplication(name = "PetClinicApplicationSmokeTests")
	@EnableEntityDefinedRegions(basePackageClasses = Pet.class)
	@SpringBootApplication(scanBasePackageClasses = PetClinicApplicationSmokeTests.class)
	static class GeodePeerTestConfiguration { }

	@Profile("petclinic-server-function-execution")
	@CacheServerApplication(name = "PetClinicApplicationSmokeTestsServer")
	@EnableEntityDefinedRegions(basePackageClasses = Pet.class)
	@EnableGemfireFunctions
	@EnablePdx
	static class GeodeServerTestConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
				new AnnotationConfigApplicationContext(GeodeServerTestConfiguration.class);

			applicationContext.registerShutdownHook();
		}

		@Bean
		PetServiceFunctions petServiceFunctions() {
			return new PetServiceFunctions();
		}
	}

	public static class PetServiceFunctions {

		@GemfireFunction(id = "AdministerPetVaccinations", optimizeForWrite = true)
		public void administerPetVaccinations(FunctionContext functionContext) {

			Optional.ofNullable(functionContext)
				.filter(RegionFunctionContext.class::isInstance)
				.map(RegionFunctionContext.class::cast)
				.map(RegionFunctionContext::getDataSet)
				.map(Region::values)
				.ifPresent(pets -> pets.forEach(pet -> {

					Pet resolvePet = (Pet) pet;

					resolvePet.vaccinate();

					((RegionFunctionContext) functionContext).getDataSet().put(resolvePet.getName(), resolvePet);
				}));
		}
	}
}
