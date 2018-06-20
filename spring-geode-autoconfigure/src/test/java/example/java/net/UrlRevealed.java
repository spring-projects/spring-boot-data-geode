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

package example.java.net;

import java.net.URL;

import org.springframework.core.io.ClassPathResource;

/**
 * The {@link UrlRevealed} class is a disclosure of Java's {@link URL} class.
 *
 * @author John Blum
 * @see java.net.URL
 * @since 1.0.0
 */
public class UrlRevealed {

	public static void main(String[] args) throws Exception {

		URL url = new URL("jar:file:///www.foo.com/bar/jar.jar!/baz/entry.txt");

		System.out.printf("URL [%s] {%n \tfile [%s],%n \tpath [%s],%n \tport [%s],%n \tprotocol [%s],%n \tquery [%s]%n}%n%n",
			url, url.getFile(), url.getPath(), url.getPort(), url.getProtocol(), url.getQuery());

		System.out.printf("URI [%s]%n", new ClassPathResource("trusted.keystore").getURL().toURI());
	}
}
