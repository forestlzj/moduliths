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
package example.events;

import static org.assertj.core.api.Assertions.*;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.Test;
import org.moduliths.events.EventPublication;
import org.moduliths.events.EventPublicationRegistry;
import org.moduliths.events.config.EnablePersistentDomainEvents;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author Oliver Gierke
 */
class PersistentDomainEventIntegrationTest {

	@Test
	void exposesEventPublicationForFailedListener() {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(ApplicationConfiguration.class, InfrastructureConfiguration.class);
		context.refresh();

		try {

			context.getBean(Client.class).method();

		} finally {

			Iterable<EventPublication> eventsToBePublished = context.getBean(EventPublicationRegistry.class)
					.findIncompletePublications();

			assertThat(eventsToBePublished).hasSize(1);

			context.close();
		}
	}

	@Configuration
	@EnablePersistentDomainEvents
	static class ApplicationConfiguration {

		@Bean
		FirstTxEventListener first() {
			return new FirstTxEventListener();
		}

		@Bean
		SecondTxEventListener second() {
			return new SecondTxEventListener();
		}

		@Bean
		ThirdTxEventListener third() {
			return new ThirdTxEventListener();
		}

		@Bean
		Client client(ApplicationEventPublisher publisher) {
			return new Client(publisher);
		}
	}

	@RequiredArgsConstructor
	static class Client {

		private final ApplicationEventPublisher publisher;

		@Transactional
		public void method() {
			publisher.publishEvent(new DomainEvent());
		}
	}

	static class DomainEvent {}

	static class NonTxEventListener {

		boolean invoked = false;

		@EventListener
		void on(DomainEvent event) {
			invoked = true;
		}
	}

	static class FirstTxEventListener {

		boolean invoked = false;

		@TransactionalEventListener
		public void on(DomainEvent event) {
			invoked = true;
		}
	}

	static class SecondTxEventListener {

		@TransactionalEventListener
		public void on(DomainEvent event) {
			throw new IllegalStateException();
		}
	}

	static class ThirdTxEventListener {

		boolean invoked = false;

		@TransactionalEventListener
		public void on(DomainEvent event) {
			invoked = true;
		}
	}
}
