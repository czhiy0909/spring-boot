/*
 * Copyright 2012-2018 the original author or authors.
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

package org.springframework.boot.task;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MyExpectedException;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for {@link TaskExecutorBuilder}.
 *
 * @author Stephane Nicoll
 */
public class TaskExecutorBuilderTests {

	@Rule
	public MyExpectedException thrown = MyExpectedException.none();

	private TaskExecutorBuilder builder = new TaskExecutorBuilder();

	@Test
	public void poolSettingsShouldApply() {
		ThreadPoolTaskExecutor executor = this.builder.queueCapacity(10).corePoolSize(4)
				.maxPoolSize(8).allowCoreThreadTimeOut(true)
				.keepAlive(Duration.ofMinutes(1)).build();
		DirectFieldAccessor dfa = new DirectFieldAccessor(executor);
		assertThat(dfa.getPropertyValue("queueCapacity")).isEqualTo(10);
		assertThat(executor.getCorePoolSize()).isEqualTo(4);
		assertThat(executor.getMaxPoolSize()).isEqualTo(8);
		assertThat(dfa.getPropertyValue("allowCoreThreadTimeOut")).isEqualTo(true);
		assertThat(executor.getKeepAliveSeconds()).isEqualTo(60);
	}

	@Test
	public void threadNamePrefixShouldApply() {
		ThreadPoolTaskExecutor executor = this.builder.threadNamePrefix("test-").build();
		assertThat(executor.getThreadNamePrefix()).isEqualTo("test-");
	}

	@Test
	public void taskDecoratorShouldApply() {
		TaskDecorator taskDecorator = mock(TaskDecorator.class);
		ThreadPoolTaskExecutor executor = this.builder.taskDecorator(taskDecorator)
				.build();
		assertThat(ReflectionTestUtils.getField(executor, "taskDecorator"))
				.isSameAs(taskDecorator);
	}

	@Test
	public void customizersWhenCustomizersAreNullShouldThrowException() {
		this.thrown.expect(IllegalArgumentException.class,
				"Customizers must not be null");
		this.builder.customizers((TaskExecutorCustomizer[]) null);
	}

	@Test
	public void customizersCollectionWhenCustomizersAreNullShouldThrowException() {
		this.thrown.expect(IllegalArgumentException.class,
				"Customizers must not be null");
		this.builder.customizers((Set<TaskExecutorCustomizer>) null);
	}

	@Test
	public void customizersShouldApply() {
		TaskExecutorCustomizer customizer = mock(TaskExecutorCustomizer.class);
		ThreadPoolTaskExecutor executor = this.builder.customizers(customizer).build();
		verify(customizer).customize(executor);
	}

	@Test
	public void customizersShouldBeAppliedLast() {
		TaskDecorator taskDecorator = mock(TaskDecorator.class);
		ThreadPoolTaskExecutor executor = spy(new ThreadPoolTaskExecutor());
		this.builder.queueCapacity(10).corePoolSize(4).maxPoolSize(8)
				.allowCoreThreadTimeOut(true).keepAlive(Duration.ofMinutes(1))
				.threadNamePrefix("test-").taskDecorator(taskDecorator)
				.additionalCustomizers((taskExecutor) -> {
					verify(taskExecutor).setQueueCapacity(10);
					verify(taskExecutor).setCorePoolSize(4);
					verify(taskExecutor).setMaxPoolSize(8);
					verify(taskExecutor).setAllowCoreThreadTimeOut(true);
					verify(taskExecutor).setKeepAliveSeconds(60);
					verify(taskExecutor).setThreadNamePrefix("test-");
					verify(taskExecutor).setTaskDecorator(taskDecorator);
				});
		this.builder.configure(executor);
	}

	@Test
	public void customizersShouldReplaceExisting() {
		TaskExecutorCustomizer customizer1 = mock(TaskExecutorCustomizer.class);
		TaskExecutorCustomizer customizer2 = mock(TaskExecutorCustomizer.class);
		ThreadPoolTaskExecutor executor = this.builder.customizers(customizer1)
				.customizers(Collections.singleton(customizer2)).build();
		verifyZeroInteractions(customizer1);
		verify(customizer2).customize(executor);
	}

	@Test
	public void additionalCustomizersWhenCustomizersAreNullShouldThrowException() {
		this.thrown.expect(IllegalArgumentException.class,
				"Customizers must not be null");
		this.builder.additionalCustomizers((TaskExecutorCustomizer[]) null);
	}

	@Test
	public void additionalCustomizersCollectionWhenCustomizersAreNullShouldThrowException() {
		this.thrown.expect(IllegalArgumentException.class,
				"Customizers must not be null");
		this.builder.additionalCustomizers((Set<TaskExecutorCustomizer>) null);
	}

	@Test
	public void additionalCustomizersShouldAddToExisting() {
		TaskExecutorCustomizer customizer1 = mock(TaskExecutorCustomizer.class);
		TaskExecutorCustomizer customizer2 = mock(TaskExecutorCustomizer.class);
		ThreadPoolTaskExecutor executor = this.builder.customizers(customizer1)
				.additionalCustomizers(customizer2).build();
		verify(customizer1).customize(executor);
		verify(customizer2).customize(executor);
	}

}
