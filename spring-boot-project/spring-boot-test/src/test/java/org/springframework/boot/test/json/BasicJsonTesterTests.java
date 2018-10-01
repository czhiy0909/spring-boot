/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.test.json;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MyExpectedException;
import org.junit.rules.TemporaryFolder;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BasicJsonTester}.
 *
 * @author Phillip Webb
 */
public class BasicJsonTesterTests {

	private static final String JSON = "{\"spring\":[\"boot\",\"framework\"]}";

	@Rule
	public MyExpectedException thrown = MyExpectedException.none();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private BasicJsonTester json = new BasicJsonTester(getClass());

	@Test
	public void createWhenResourceLoadClassIsNullShouldThrowException() {
		this.thrown.expect(IllegalArgumentException.class,
				"ResourceLoadClass must not be null", () -> new BasicJsonTester(null));
	}

	@Test
	public void fromJsonStringShouldReturnJsonContent() {
		assertThat(this.json.from(JSON)).isEqualToJson("source.json");
	}

	@Test
	public void fromResourceStringShouldReturnJsonContent() {
		assertThat(this.json.from("source.json")).isEqualToJson(JSON);
	}

	@Test
	public void fromResourceStringWithClassShouldReturnJsonContent() {
		assertThat(this.json.from("source.json", getClass())).isEqualToJson(JSON);
	}

	@Test
	public void fromByteArrayShouldReturnJsonContent() {
		assertThat(this.json.from(JSON.getBytes())).isEqualToJson("source.json");
	}

	@Test
	public void fromFileShouldReturnJsonContent() throws Exception {
		File file = this.tempFolder.newFile("file.json");
		FileCopyUtils.copy(JSON.getBytes(), file);
		assertThat(this.json.from(file)).isEqualToJson("source.json");
	}

	@Test
	public void fromInputStreamShouldReturnJsonContent() {
		InputStream inputStream = new ByteArrayInputStream(JSON.getBytes());
		assertThat(this.json.from(inputStream)).isEqualToJson("source.json");
	}

	@Test
	public void fromResourceShouldReturnJsonContent() {
		Resource resource = new ByteArrayResource(JSON.getBytes());
		assertThat(this.json.from(resource)).isEqualToJson("source.json");
	}

}
