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
package example.app.caching.multisite.client.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.lang.NonNull;

/**
 * Abstract utility class for {@link Thread Threading}.
 *
 * @author John Blum
 * @see java.lang.Thread
 * @see java.time.Duration
 * @since 1.3.0
 */
public abstract class ThreadUtils {

	@SuppressWarnings("all")
	public static void safeSleep(@NonNull Object lock, @NonNull Duration duration) {

		boolean interrupted = false;

		long timeout = System.currentTimeMillis() + duration.toMillis();

		while (System.currentTimeMillis() < timeout) {
			synchronized (lock) {
				try {
					lock.wait(duration.toMillis(), 0);
				}
				catch (InterruptedException ignore) {
					interrupted = true;
				}
			}
		}

		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	public static boolean waitFor(@NonNull Duration duration, @NonNull Condition condition) {
		return waitFor(duration, duration.toMillis() / 10L, condition);
	}

	@SuppressWarnings("all")
	public static boolean waitFor(Duration duration, long interval, Condition condition) {

		long timeout = System.currentTimeMillis() + duration.toMillis();

		try {
			while (!condition.evaluate() && System.currentTimeMillis() < timeout) {
				synchronized (condition) {
					TimeUnit.MILLISECONDS.timedWait(condition, interval);
				}
			}
		}
		catch (InterruptedException cause) {
			Thread.currentThread().interrupt();
		}

		return condition.evaluate();
	}

	@FunctionalInterface
	public interface Condition {
		boolean evaluate();
	}
}
