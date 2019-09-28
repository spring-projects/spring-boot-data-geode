/*
 * Copyright 2019 the original author or authors.
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
package example.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Smoke Tests for {@link InitializerApplication}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.InitializerApplication
 * @since 1.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@SuppressWarnings("unused")
public class InitializerApplicationSmokeTests {

	@Autowired
	@Qualifier("exampleTemplate")
	private GemfireTemplate exampleTemplate;

	@Test
	public void loadsContextAndRegionDataAccessOperationsWork() {

		assertThat(this.exampleTemplate).isNotNull();
		assertThat(this.exampleTemplate.getRegion()).isNotNull();
		assertThat(this.exampleTemplate.getRegion().getName()).isEqualTo("Example");
		assertThat(this.exampleTemplate.put(1, "TEST")).isNull();
		assertThat(this.exampleTemplate.<Integer, String>get(1)).isEqualTo("TEST");
	}
}
