/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.generator.java.object;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.generator.java.FluentJavaClassGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.generator.java.types.RJavaFieldWithMeta;
import com.regnosys.rosetta.generator.java.types.RJavaReferenceWithMeta;
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.WithMetaOperation;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.rosetta.model.lib.meta.BasicRosettaMetaData;
import com.rosetta.util.types.JavaGenericTypeDeclaration;
import com.rosetta.util.types.JavaParameterizedType;

import jakarta.inject.Inject;

public class MetaFieldGenerator extends FluentJavaClassGenerator<RMetaAnnotatedType, RJavaWithMetaValue> {
	@Inject
	private ModelObjectGenerator modelObjectGenerator;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private RosettaTypeProvider typeProvider;

	@Override
	protected EObject getSource(RMetaAnnotatedType object) {
		return null;
	}

	@Override
	protected Stream<RMetaAnnotatedType> streamObjects(RosettaModel model) {
		return streamObjects((EObject) model);
	}

	public Stream<RMetaAnnotatedType> streamObjects(EObject model) {
		return Stream.concat(
					EcoreUtil2.eAllOfType(model, Attribute.class).stream()
							.map(attr -> rObjectFactory.buildRAttribute(attr).getRMetaAnnotatedType())
							.filter(RMetaAnnotatedType::hasAttributeMeta),
					EcoreUtil2.eAllOfType(model, WithMetaOperation.class).stream()
							.map(typeProvider::getRMetaAnnotatedType)
							.filter(RMetaAnnotatedType::hasAttributeMeta))
				.distinct()
				.filter(t -> typeTranslator.toJavaReferenceType(t) instanceof RJavaWithMetaValue);
	}

	@Override
	public RJavaWithMetaValue createTypeRepresentation(RMetaAnnotatedType t) {
		return (RJavaWithMetaValue) typeTranslator.toJavaReferenceType(t);
	}

	@Override
	public CodeRenderer generateClass(RMetaAnnotatedType t, RJavaWithMetaValue metaJt, String version, JavaClassScope scope) {
		if (!(metaJt instanceof RJavaReferenceWithMeta) && !(metaJt instanceof RJavaFieldWithMeta)) {
			throw new UnsupportedOperationException("Invalid JavaType: " + metaJt);
		}
		RGeneratedJavaClass<?> dummyMetaClass = createAndRegisterDummyMetaClass(metaJt, scope);
		return out -> {
			out.write(modelObjectGenerator.classBody(metaJt, scope, dummyMetaClass, "1"));
			out.newline();
			out.writeln(dummyMetaClass.asClassDeclaration(), " {");
			out.newline();
			out.writeln("}");
		};
	}

	private RGeneratedJavaClass<?> createAndRegisterDummyMetaClass(RJavaWithMetaValue metaJavaType, JavaClassScope scope) {
		JavaParameterizedType<BasicRosettaMetaData> metaDataInterface = JavaParameterizedType.from(JavaGenericTypeDeclaration.from(BasicRosettaMetaData.class), metaJavaType);
		RGeneratedJavaClass<?> dummyMetaClass = RGeneratedJavaClass.createWithSuperclass(metaJavaType.getEscapedPackageName(), metaJavaType.getSimpleName() + "Meta", metaDataInterface);
		scope.getFileScope().createIdentifier(dummyMetaClass, dummyMetaClass.getSimpleName());
		return dummyMetaClass;
	}
}
