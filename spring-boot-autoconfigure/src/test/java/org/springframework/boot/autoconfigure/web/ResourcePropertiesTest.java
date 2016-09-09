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

package org.springframework.boot.autoconfigure.web;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link ResourceProperties}.
 *
 * @author Stephane Nicoll
 */
public class ResourcePropertiesTest {

	private final ResourceProperties properties = new ResourceProperties();

	@Test
	public void resourceChainNoCustomization() {
		System.out.println(this.properties.getChain().getEnabled());
		assertThat(this.properties.getChain().getEnabled(), nullValue());
	}

	@Test
	public void resourceChainStrategyEnabled() {
		this.properties.getChain().getStrategy().getFixed().setEnabled(true);
		assertThat(this.properties.getChain().getEnabled(), equalTo(true));
	}

	@Test
	public void resourceChainEnabled() {
		this.properties.getChain().setEnabled(true);
		assertThat(this.properties.getChain().getEnabled(), equalTo(true));
	}

	@Test
	public void resourceChainDisabled() {
		this.properties.getChain().setEnabled(false);
		assertThat(this.properties.getChain().getEnabled(), equalTo(false));
	}

}
