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

package org.springframework.geode.function.support;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import org.apache.geode.cache.execute.ResultCollector;
import org.apache.geode.distributed.DistributedMember;

/**
 * The {@link SingleResultReturningCollector} class is an implementation of the {@link ResultCollector} interface
 * which returns a single {@link Object result}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.execute.ResultCollector
 * @see org.springframework.geode.function.support.AbstractResultCollector
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SingleResultReturningCollector<T> extends AbstractResultCollector<T, T> {

	@Override
	public void addResult(DistributedMember memberID, T resultOfSingleExecution) {
		setResult(extractSingleResult(resultOfSingleExecution));
	}

	@SuppressWarnings("unchecked")
	private <T> T extractSingleResult(Object result) {

		return (T) Optional.ofNullable(result)
			.filter(this::isInstanceOfIterableOrIterator)
			.map(this::toIterator)
			.filter(Iterator::hasNext)
			.map(Iterator::next)
			.map(this::extractSingleResult)
			.orElseGet(() -> isInstanceOfIterableOrIterator(result) ? null : result);
	}

	private boolean isInstanceOfIterableOrIterator(Object obj) {
		return obj instanceof Iterable || obj instanceof Iterator;
	}

	@SuppressWarnings("unchecked")
	private <T> Iterator<T> toIterator(Object obj) {
		return obj instanceof Iterator ? (Iterator<T>) obj : toIterator((Iterable<T>) obj);
	}

	private <T> Iterator<T> toIterator(Iterable<T> iterable) {
		return iterable != null ? iterable.iterator() : Collections.emptyIterator();
	}
}
