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

package org.springframework.data.gemfire.function.execution;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import org.apache.geode.distributed.DistributedMember;

/**
 * The SingleResultReturningCollector class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class SingleResultReturningCollector<T> extends AbstractResultCollector<T, T> {

	@Override
	public void addResult(DistributedMember memberID, T resultOfSingleExecution) {
		setResult(extractSingleResult(resultOfSingleExecution));
	}

	@SuppressWarnings("unchecked")
	private <T> T extractSingleResult(Object result) {

		return (T) Optional.ofNullable(result)
			.filter(this::isInstanceOfIterable)
			.map(it -> (Iterable<T>) it)
			.map(this::toIterator)
			.filter(Iterator::hasNext)
			.map(Iterator::next)
			.map(this::extractSingleResult)
			.orElse(result);
	}

	private boolean isInstanceOfIterable(Object obj) {
		return obj instanceof Iterable;
	}

	private <T> Iterator<T> toIterator(Iterable<T> obj) {
		return obj != null ? obj.iterator() : Collections.emptyIterator();
	}
}
