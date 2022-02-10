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

import org.apache.geode.distributed.Locator;
import org.apache.geode.distributed.LocatorLauncher;

/**
 * An Apache Geode {@link Locator} application configured and bootstrapped using the Apache Geode API.
 *
 * @author John Blum
 * @see org.apache.geode.distributed.Locator
 * @see org.apache.geode.distributed.LocatorLauncher
 * @since 1.3.2
 */
public class ApacheGeodeLocatorApplication {

	public static void main(String[] args) {

		LocatorLauncher locatorLauncher = new LocatorLauncher.Builder()
			.set("jmx-manager", "true")
			.set("jmx-manager-port", "0")
			.set("jmx-manager-start", "true")
			.setMemberName("ApacheGeodeBasedLocator")
			.setPort(0)
			.build();

		locatorLauncher.start();

		//locatorLauncher.waitOnLocator();
	}
}
