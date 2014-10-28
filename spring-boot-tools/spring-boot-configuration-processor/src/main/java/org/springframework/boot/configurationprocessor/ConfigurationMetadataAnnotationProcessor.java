/*
 * Copyright 2012-2014 the original author or authors.
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

package org.springframework.boot.configurationprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.GroupMetadata;
import org.springframework.boot.configurationprocessor.metadata.JsonMarshaller;
import org.springframework.boot.configurationprocessor.metadata.PropertyMetadata;

/**
 * Annotation {@link Processor} that writes meta-data file for
 * {@code @ConfigurationProperties}.
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 1.2.0
 */
@SupportedAnnotationTypes({ ConfigurationMetadataAnnotationProcessor.ANNOTATION })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ConfigurationMetadataAnnotationProcessor extends AbstractProcessor {

	static final String ANNOTATION = "org.springframework.boot.context.properties.ConfigurationProperties";

	private ConfigurationMetadata metadata;

	private TypeUtils typeUtils;

	protected String annotationType() {
		return ANNOTATION;
	}

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);
		this.metadata = new ConfigurationMetadata();
		this.typeUtils = new TypeUtils(env);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		for (TypeElement annotation : annotations) {
			for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
				processElement(element);
			}
		}
		if (roundEnv.processingOver()) {
			writeMetaData(this.metadata);
		}
		return false;
	}

	private void processElement(Element element) {
		AnnotationMirror annotation = getAnnotation(element);
		String prefix = getPrefix(annotation);
		if (annotation != null) {
			if (element instanceof TypeElement) {
				processAnnotatedTypeElement(prefix, (TypeElement) element);
			}
			else if (element instanceof ExecutableElement) {
				processExecutableElement(prefix, (ExecutableElement) element);
			}
		}
	}

	private void processAnnotatedTypeElement(String prefix, TypeElement element) {
		this.metadata.add(new GroupMetadata(prefix, null,
				this.typeUtils.getType(element), null, null));
		processTypeElement(prefix, element);
	}

	private void processExecutableElement(String prefix, ExecutableElement element) {
		if (element.getModifiers().contains(Modifier.PUBLIC)
				&& (TypeKind.VOID != element.getReturnType().getKind())) {
			Element returns = this.processingEnv.getTypeUtils().asElement(
					element.getReturnType());
			if (returns instanceof TypeElement) {
				this.metadata
						.add(new GroupMetadata(prefix, null, this.typeUtils
								.getType(element.getEnclosingElement()), element
								.toString(), null));
				processTypeElement(prefix, (TypeElement) returns);
			}
		}
	}

	private void processTypeElement(String prefix, TypeElement element) {
		TypeElementMembers members = new TypeElementMembers(this.processingEnv, element);
		processSimpleTypes(prefix, element, members);
		processNestedTypes(prefix, element, members);
	}

	private void processSimpleTypes(String prefix, TypeElement element,
			TypeElementMembers members) {
		for (Map.Entry<String, ExecutableElement> entry : members.getPublicGetters()
				.entrySet()) {
			String name = entry.getKey();
			ExecutableElement getter = entry.getValue();
			ExecutableElement setter = members.getPublicSetters().get(name);
			VariableElement field = members.getFields().get(name);
			if (setter != null
					|| this.typeUtils.isCollectionOrMap(getter.getReturnType())) {
				String dataType = this.typeUtils.getType(getter.getReturnType());
				String sourceType = this.typeUtils.getType(element);
				String description = this.typeUtils.getJavaDoc(field);
				this.metadata.add(new PropertyMetadata(prefix, name, dataType,
						sourceType, null, description));
			}
		}
	}

	private void processNestedTypes(String prefix, TypeElement element,
			TypeElementMembers members) {
		for (Map.Entry<String, ExecutableElement> entry : members.getPublicGetters()
				.entrySet()) {
			ExecutableElement getter = entry.getValue();
			Element returnType = this.processingEnv.getTypeUtils().asElement(
					getter.getReturnType());
			AnnotationMirror annotation = getAnnotation(getter);
			if (returnType != null && returnType instanceof TypeElement
					&& annotation == null) {
				TypeElement returns = (TypeElement) returnType;
				if (this.typeUtils.isEnclosedIn(returnType, element)) {
					String nestedPrefix = ConfigurationMetadata.nestedPrefix(prefix,
							entry.getKey());
					this.metadata.add(new GroupMetadata(nestedPrefix, null,
							this.typeUtils.getType(returns), getter.toString(), null));
					processTypeElement(nestedPrefix, returns);
				}
			}
		}
	}

	private AnnotationMirror getAnnotation(Element element) {
		for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
			if (annotationType().equals(annotation.getAnnotationType().toString())) {
				return annotation;
			}
		}
		return null;
	}

	private String getPrefix(AnnotationMirror annotation) {
		Map<String, Object> elementValues = getAnnotationElementValues(annotation);
		Object prefix = elementValues.get("prefix");
		if (prefix != null && !"".equals(prefix)) {
			return (String) prefix;
		}
		Object value = elementValues.get("value");
		if (value != null && !"".equals(value)) {
			return (String) value;
		}
		return null;
	}

	private Map<String, Object> getAnnotationElementValues(AnnotationMirror annotation) {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation
				.getElementValues().entrySet()) {
			values.put(entry.getKey().getSimpleName().toString(), entry.getValue()
					.getValue());
		}
		return values;
	}

	protected void writeMetaData(ConfigurationMetadata metadata) {
		metadata = mergeManualMetadata(metadata);
		try {
			FileObject resource = this.processingEnv.getFiler().createResource(
					StandardLocation.CLASS_OUTPUT, "",
					"META-INF/spring-configuration-metadata.json");
			OutputStream outputStream = resource.openOutputStream();
			try {
				new JsonMarshaller().write(metadata, outputStream);
			}
			finally {
				outputStream.close();
			}
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private ConfigurationMetadata mergeManualMetadata(ConfigurationMetadata metadata) {
		try {
			FileObject manualMetadata = this.processingEnv.getFiler().getResource(
					StandardLocation.CLASS_PATH, "",
					"META-INF/additional-spring-configuration-metadata.json");
			InputStream inputStream = manualMetadata.openInputStream();
			try {
				ConfigurationMetadata merged = new ConfigurationMetadata(metadata);
				try {
					merged.addAll(new JsonMarshaller().read(inputStream));
				}
				catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
				return merged;
			}
			finally {
				inputStream.close();
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return metadata;
		}
	}

}
