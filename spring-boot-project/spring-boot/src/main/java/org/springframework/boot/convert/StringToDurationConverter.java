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

package org.springframework.boot.convert;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.ObjectUtils;

/**
 * {@link Converter} to convert from a {@link String} to a {@link Duration}. Supports
 * {@link Duration#parse(CharSequence)} as well a more readable {@code 10s} form.
 *
 * @author Phillip Webb
 * @see DurationFormat
 * @see DurationUnit
 */
final class StringToDurationConverter implements GenericConverter {

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(String.class, Duration.class));
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType,
			TypeDescriptor targetType) {
		if (ObjectUtils.isEmpty(source)) {
			return null;
		}
		DurationFormat format = targetType.getAnnotation(DurationFormat.class);
		DurationUnit defaultUnit = targetType.getAnnotation(DurationUnit.class);
		return toDuration(source.toString(), (format == null ? null : format.value()),
				(defaultUnit == null ? null : defaultUnit.value()));
	}

	private Duration toDuration(String source, DurationStyle style,
			ChronoUnit defaultUnit) {
		style = (style != null ? style : DurationStyle.detect(source));
		return style.parse(source, defaultUnit);
	}

}
