/*
 * Copyright 2012-2016 the original author or authors.
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

package org.springframework.boot.autoconfigure.ws;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WsAutoConfiguration}.
 *
 * @author Vedran Pavic
 */
public class WsAutoConfigurationTests {

	private AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setupContext() {
		this.context.setServletContext(new MockServletContext());
	}

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void defaultConfiguration() {
		registerAndRefresh(WsAutoConfiguration.class);

		assertThat(this.context.getBeansOfType(ServletRegistrationBean.class)).hasSize(1);
	}

	@Test
	public void customPathMustBeginWithASlash() {
		this.thrown.expect(BeanCreationException.class);
		this.thrown.expectMessage("Path must start with /");
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.ws.path=invalid");
		registerAndRefresh(WsAutoConfiguration.class);
	}

	@Test
	public void customPathWithTrailingSlash() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.ws.path=/valid/");
		registerAndRefresh(WsAutoConfiguration.class);

		assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings())
				.contains("/valid/*");
	}

	@Test
	public void customPath() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.ws.path=/valid");
		registerAndRefresh(WsAutoConfiguration.class);

		assertThat(this.context.getBeansOfType(ServletRegistrationBean.class)).hasSize(1);
		assertThat(this.context.getBean(ServletRegistrationBean.class).getUrlMappings())
				.contains("/valid/*");
	}

	@Test
	public void customLoadOnStartup() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.ws.servlet.load-on-startup=1");
		registerAndRefresh(WsAutoConfiguration.class);

		ServletRegistrationBean registrationBean = this.context
				.getBean(ServletRegistrationBean.class);
		assertThat(ReflectionTestUtils.getField(registrationBean, "loadOnStartup"))
				.isEqualTo(1);
	}

	@Test
	public void customInitParameters() {
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.ws.init.key1=value1", "spring.ws.init.key2=value2");
		registerAndRefresh(WsAutoConfiguration.class);

		ServletRegistrationBean registrationBean = this.context
				.getBean(ServletRegistrationBean.class);
		assertThat(registrationBean.getInitParameters()).containsEntry("key1", "value1");
		assertThat(registrationBean.getInitParameters()).containsEntry("key2", "value2");
	}

	private void registerAndRefresh(Class<?>... annotatedClasses) {
		this.context.register(annotatedClasses);
		this.context.refresh();
	}

}
