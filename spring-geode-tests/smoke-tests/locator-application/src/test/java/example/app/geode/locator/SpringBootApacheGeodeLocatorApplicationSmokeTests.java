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
package example.app.geode.locator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.DistributedSystem;
import org.apache.geode.distributed.Locator;
import org.apache.geode.distributed.LocatorLauncher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.GemFireProperties;
import org.springframework.data.gemfire.config.annotation.LocatorApplicationConfiguration;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Smoke Tests for {@link SpringBootApacheGeodeLocatorApplication}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.apache.geode.distributed.Locator
 * @see org.apache.geode.distributed.LocatorLauncher
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.config.annotation.LocatorApplication
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.3.0
 */
@ActiveProfiles("locator-configurer")
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootApacheGeodeLocatorApplicationSmokeTests extends ForkingClientServerIntegrationTestsSupport {

	private static final String SPRING_DATA_GEMFIRE_LOCATORS_PROPERTY = "spring.data.gemfire.locators";

	@Autowired
	@SuppressWarnings("unused")
	private Locator locator;

	@BeforeClass
	public static void startGeodeLocator() throws IOException {

		int locatorPort = findAvailablePort();

		startGemFireServer(ApacheGeodeLocator.class, String.valueOf(locatorPort));

		System.setProperty(SPRING_DATA_GEMFIRE_LOCATORS_PROPERTY,
			String.format("%1$s[%2$d]", ApacheGeodeLocator.HOSTNAME_FOR_CLIENTS, locatorPort));
	}

	@AfterClass
	public static void afterGeodeLocatorStop() {
		System.clearProperty(SPRING_DATA_GEMFIRE_LOCATORS_PROPERTY);
	}

	@Test
	public void geodeAndSpringLocatorsArePresent() {

		assertThat(this.locator).isNotNull();

		DistributedSystem distributedSystem = this.locator.getDistributedSystem();

		assertThat(distributedSystem).isNotNull();
		assertThat(distributedSystem.getAllOtherMembers()).hasSize(1);

		DistributedMember springLocatorMember = distributedSystem.getDistributedMember();

		assertThat(springLocatorMember).isNotNull();
		assertThat(springLocatorMember.getName()).isEqualTo(SpringBootApacheGeodeLocatorApplication.class.getSimpleName());

		DistributedMember geodeLocatorMember = distributedSystem.getAllOtherMembers().iterator().next();

		assertThat(geodeLocatorMember).isNotNull();
		assertThat(geodeLocatorMember.getName()).isEqualTo(ApacheGeodeLocator.MEMBER_NAME);
	}

	public static class ApacheGeodeLocator {

		private static final boolean DEBUG = false;
		private static final boolean DELETE_PID_FILE_ON_STOP = true;
		private static final boolean ENABLE_CLUSTER_CONFIGURATION = false;

		private static final int LOCATOR_DEFAULT_PORT = LocatorApplicationConfiguration.DEFAULT_PORT;

		private static final String HOSTNAME_FOR_CLIENTS = "localhost";
		private static final String MEMBER_NAME = ApacheGeodeLocator.class.getSimpleName();
		private static final String WORKING_DIRECTORY = ".";

		public static void main(String[] args) {

			int locatorPort = resolveLocatorPort(args);

			LocatorLauncher locatorLauncher = buildLocatorLauncher(locatorPort);

			locatorLauncher.start();
			locatorLauncher.waitOnLocator();
		}

		private static int resolveLocatorPort(String[] args) {
			return ArrayUtils.isNotEmpty(args) ? Integer.parseInt(args[0]) : LOCATOR_DEFAULT_PORT;
		}

		private static LocatorLauncher buildLocatorLauncher(int port) {

			return new LocatorLauncher.Builder()
				.setDeletePidFileOnStop(DELETE_PID_FILE_ON_STOP)
				.setDebug(DEBUG)
				.setHostnameForClients(HOSTNAME_FOR_CLIENTS)
				.setPort(port)
				.setMemberName(MEMBER_NAME)
				.setWorkingDirectory(WORKING_DIRECTORY)
				.set(GemFireProperties.ENABLE_CLUSTER_CONFIGURATION.toString(),
					String.valueOf(ENABLE_CLUSTER_CONFIGURATION))
				.build();
		}
	}
}
