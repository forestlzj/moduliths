/*
 * Copyright 2017-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.moduliths.events.config;

import org.moduliths.events.EventPublicationRegistry;
import org.moduliths.events.support.MapEventPublicationRegistry;
import org.moduliths.events.support.PersistentApplicationEventMulticaster;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Oliver Drotbohm
 */
@Configuration(proxyBeanMethods = false)
class EventPublicationConfiguration {

	@Bean
	PersistentApplicationEventMulticaster applicationEventMulticaster(ObjectProvider<EventPublicationRegistry> registry) {

		return new PersistentApplicationEventMulticaster(
				() -> registry.getIfAvailable(() -> new MapEventPublicationRegistry()));
	}
}
