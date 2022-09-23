/*
 * Copyright 2012-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.tracing;

import java.util.function.Supplier;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.opentelemetry.context.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.MDC;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Baggage configuration.
 *
 * @author Marcin Grzejszczak
 */
class MdcAutoConfigurationTests {

	@BeforeEach
	@AfterEach
	void setup() {
		MDC.clear();
	}

	@ParameterizedTest
	@EnumSource(AutoConfig.class)
	void shouldSetEntriesToMdcFromSpan(AutoConfig autoConfig) {
		autoConfig.get().run((context) -> {
			Tracer tracer = tracer(context);
			Span span = createSpan(tracer);
			assertThatTracingContextIsInitialized(autoConfig);
			try (Tracer.SpanInScope scope = tracer.withSpan(span.start())) {
				assertThat(MDC.get("traceId")).isEqualTo(span.context().traceId());
			}
			finally {
				span.end();
			}
			assertThatMdcContainsUnsetTraceId();
		});
	}

	@ParameterizedTest
	@EnumSource(AutoConfig.class)
	void shouldRemoveEntriesFromMdcFromNullSpan(AutoConfig autoConfig) {
		autoConfig.get().run((context) -> {
			Tracer tracer = tracer(context);
			Span span = createSpan(tracer);
			assertThatTracingContextIsInitialized(autoConfig);
			// can't use NOOP as it is special cased
			try (Tracer.SpanInScope scope = tracer.withSpan(span.start())) {
				assertThat(MDC.get("traceId")).isEqualTo(span.context().traceId());

				// can't use NOOP as it is special cased
				try (Tracer.SpanInScope scope2 = tracer.withSpan(null)) {
					assertThatMdcContainsUnsetTraceId();
				}

				assertThat(MDC.get("traceId")).isEqualTo(span.context().traceId());
			}
			finally {
				span.end();
			}
			assertThatMdcContainsUnsetTraceId();

		});

	}

	private Span createSpan(Tracer tracer) {
		return tracer.nextSpan().name("span");
	}

	private Tracer tracer(ApplicationContext context) {
		return context.getBean(Tracer.class);
	}

	private void assertThatTracingContextIsInitialized(AutoConfig autoConfig) {
		if (autoConfig == AutoConfig.OTEL) {
			assertThat(Context.current()).isEqualTo(Context.root());
		}
	}

	private void assertThatMdcContainsUnsetTraceId() {
		assertThat(isInvalidBraveTraceId() || isInvalidOtelTraceId()).isTrue();
	}

	private boolean isInvalidBraveTraceId() {
		return MDC.get("traceId") == null;
	}

	private boolean isInvalidOtelTraceId() {
		return MDC.get("traceId").equals("00000000000000000000000000000000");
	}

	enum AutoConfig implements Supplier<ApplicationContextRunner> {

		BRAVE {
			@Override
			public ApplicationContextRunner get() {
				return new ApplicationContextRunner()
						.withConfiguration(AutoConfigurations.of(BraveAutoConfiguration.class));
			}
		},

		OTEL {
			@Override
			public ApplicationContextRunner get() {
				return new ApplicationContextRunner()
						.withConfiguration(AutoConfigurations.of(OpenTelemetryAutoConfiguration.class));
			}
		}

	}

}
