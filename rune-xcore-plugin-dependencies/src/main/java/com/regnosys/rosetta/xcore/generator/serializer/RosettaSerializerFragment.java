package com.regnosys.rosetta.xcore.generator.serializer;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.codegen.ecore.genmodel.GenFeature;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.Parameter;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.serializer.ISerializationContext;
import org.eclipse.xtext.serializer.acceptor.SequenceFeeder;
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider.IConstraint;
import org.eclipse.xtext.serializer.analysis.ISemanticSequencerNfaProvider.ISemState;
import org.eclipse.xtext.serializer.analysis.SerializationContext;
import org.eclipse.xtext.serializer.sequencer.AbstractDelegatingSemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService.ValueTransient;
import org.eclipse.xtext.xtext.generator.grammarAccess.GrammarAccessExtensions;
import org.eclipse.xtext.xtext.generator.model.FileAccessFactory;
import org.eclipse.xtext.xtext.generator.model.GeneratedJavaFileAccess;
import org.eclipse.xtext.xtext.generator.model.TypeReference;
import org.eclipse.xtext.xtext.generator.model.annotations.SuppressWarningsAnnotation;
import org.eclipse.xtext.xtext.generator.serializer.SemanticSequencerExtensions;
import org.eclipse.xtext.xtext.generator.serializer.SerializerFragment2;
import org.eclipse.xtext.xtext.generator.util.GenModelUtil2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

import jakarta.inject.Inject;

/**
 * This custom serializer is designed to prevent the semantic sequencer from generating overly large
 * sequence methods in Java, which can cause compiler errors. It achieves this by encapsulating the
 * conditional logic of each case statement into separate methods, which are then invoked within the
 * respective case statements.
 */
public class RosettaSerializerFragment extends SerializerFragment2 {
	private static final Logger LOG = LoggerFactory.getLogger(RosettaSerializerFragment.class);

	private record MethodSignature(String name, EClass type) {
	}

	@Inject
	private SemanticSequencerExtensions semanticSequencerExtensions;
	@Inject
	private GrammarAccessExtensions grammarAccessExtensions;
	@Inject
	private FileAccessFactory fileAccessFactory;

	@Override
	public void generateAbstractSemanticSequencer() {
		Grammar grammar = getGrammar();
		Collection<IConstraint> localConstraints = semanticSequencerExtensions.getGrammarConstraints(grammar);
		Collection<IConstraint> superConstraints = semanticSequencerExtensions.getGrammarConstraints(semanticSequencerExtensions.getSuperGrammar(grammar));
		List<IConstraint> newLocalConstraints = localConstraints.stream()
				.filter(constraint -> constraint.getType() != null && !superConstraints.contains(constraint))
				.distinct()
				.sorted()
				.toList();
		TypeReference clazz = isGenerateStub() ? getAbstractSemanticSequencerClass(grammar) : getSemanticSequencerClass(grammar);
		TypeReference superClazz = localConstraints.stream().anyMatch(superConstraints::contains)
				? getSemanticSequencerClass(Iterables.getFirst(grammar.getUsedGrammars(), null))
				: TypeReference.typeRef(AbstractDelegatingSemanticSequencer.class);
		GeneratedJavaFileAccess javaFile = fileAccessFactory.createGeneratedJavaFile(clazz);
		javaFile.setResourceSet(getLanguage().getResourceSet());
		Set<MethodSignature> methodSignatures = new HashSet<>();

		// On duplicates the last constraint wins.
		Map<IConstraint, IConstraint> superConstraintsMap = superConstraints.stream()
				.collect(Collectors.toMap(Function.identity(), Function.identity(), (first, last) -> last, LinkedHashMap::new));

		javaFile.setContent(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("public ");
				if (isGenerateStub()) {
					target.append(" abstract ");
				}
				target.append("class ");
				target.append(clazz.getSimpleName());
				target.append(" extends ");
				target.append(superClazz);
				target.append(" {\n\n\t@");
				target.append(Inject.class);
				target.append("\n\tprivate ");
				target.append(grammarAccessExtensions.getGrammarAccess(grammar));
				target.append(" grammarAccess;\n\t\n\t");
				target.append(genMethodCreateSequence(superConstraintsMap), "\t");
				target.newLineIfNotEmpty();
				target.append("\t\n");
				for (StringConcatenationClient conditionMethod : genConditionMethods()) {
					target.append("\t");
					target.append(conditionMethod, "\t");
					target.newLineIfNotEmpty();
					target.append("\t\n");
				}
				target.append("\t\n");
				for (IConstraint constraint : newLocalConstraints) {
					target.append("\t");
					if (methodSignatures.add(new MethodSignature(constraint.getSimpleName(), constraint.getType()))) {
						target.append(genMethodSequence(constraint), "\t");
					} else {
						LOG.warn("Skipped generating duplicate method in " + clazz.getSimpleName());
						target.append(genMethodSequenceComment(constraint), "\t");
					}
					target.newLineIfNotEmpty();
					target.append("\t\n");
				}
				target.append("}\n");
			}
		});
		javaFile.getAnnotations().add(new SuppressWarningsAnnotation());
		javaFile.writeTo(getProjectConfig().getRuntime().getSrcGen());
	}

	private List<EPackage> getAccessedPackages() {
		return semanticSequencerExtensions.getGrammarConstraints(getGrammar()).stream()
				.map(IConstraint::getType)
				.filter(Objects::nonNull)
				.map(EClass::getEPackage)
				.distinct()
				.sorted(Comparator.comparing(EPackage::getName))
				.toList();
	}

	private List<EClass> getAccessedClasses(EPackage pkg) {
		return semanticSequencerExtensions.getGrammarConstraints(getGrammar()).stream()
				.map(IConstraint::getType)
				.filter(type -> type != null && pkg.equals(type.getEPackage()))
				.distinct()
				.sorted(Comparator.comparing(EClass::getName))
				.toList();
	}

	private List<Map.Entry<IConstraint, List<ISerializationContext>>> getConstraintContexts(EClass type) {
		return semanticSequencerExtensions.getGrammarConstraints(getGrammar(), type).entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().getName()))
				.toList();
	}

	private StringConcatenationClient genMethodCreateSequence(Map<IConstraint, IConstraint> superConstraints) {
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("@Override\npublic void sequence(");
				target.append(ISerializationContext.class);
				target.append(" context, ");
				target.append(EObject.class);
				target.append(" semanticObject) {\n\t");
				target.append(EPackage.class);
				target.append(" epackage = semanticObject.eClass().getEPackage();\n\t");
				target.append(ParserRule.class);
				target.append(" rule = context.getParserRule();\n\t");
				target.append(Action.class);
				target.append(" action = context.getAssignedAction();\n\t");
				target.append(Set.class);
				target.append("<");
				target.append(Parameter.class);
				target.append("> parameters = context.getEnabledBooleanParameters();\n");
				List<EPackage> packages = getAccessedPackages();
				for (int i = 0; i < packages.size(); i++) {
					EPackage pkg = packages.get(i);
					target.append("\t");
					if (i > 0) {
						target.append("else ");
					}
					target.append("if (epackage == ");
					target.append(pkg);
					target.append(".");
					target.append(GenModelUtil2.getPackageLiteral());
					target.append(")\n\t\tswitch (semanticObject.eClass().getClassifierID()) {\n");
					for (EClass type : getAccessedClasses(pkg)) {
						target.append("\t\tcase ");
						target.append(pkg);
						target.append(".");
						target.append(GenModelUtil2.getIntLiteral(type, getLanguage().getResourceSet()));
						target.append(":\n\t\t\t");
						target.append(genMethodCreateSequenceCaseBody(superConstraints, type), "\t\t\t");
						target.newLineIfNotEmpty();
					}
					target.append("\t\t}\n");
				}
				target.append("""
						\tif (errorAcceptor != null)
						\t\terrorAcceptor.accept(diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context));
						}
						""");
			}
		};
	}

	private StringConcatenationClient genParameterCondition(ISerializationContext context, IConstraint constraint) {
		Set<Parameter> values = context.getEnabledBooleanParameters();
		if (!values.isEmpty()) {
			String parameters = values.stream()
					.map(parameter -> "grammarAccess." + grammarAccessExtensions.gaAccessor(parameter))
					.collect(joining(", "));
			return inline(ImmutableSet.class, ".of(", parameters, ").equals(parameters)");
		} else if (constraint.getContexts().stream().anyMatch(ctx -> !((SerializationContext) ctx).getDeclaredParameters().isEmpty())) {
			return inline("parameters.isEmpty()");
		} else {
			return inline();
		}
	}

	private StringConcatenationClient genMethodCreateSequenceCaseBody(Map<IConstraint, IConstraint> superConstraints, EClass type) {
		List<Map.Entry<IConstraint, List<ISerializationContext>>> contexts = getConstraintContexts(type);
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				if (contexts.size() > 1) {
					for (int i = 0; i < contexts.size(); i++) {
						IConstraint constraint = contexts.get(i).getKey();
						if (i > 0) {
							target.append("else ");
						}
						target.append("if (");
						target.append(genConditionMethodCall(constraint));
						target.append(") {\n\t");
						target.append(genMethodCreateSequenceCall(superConstraints, type, constraint), "\t");
						target.newLineIfNotEmpty();
						target.append("}\n");
					}
					target.append("else break;\n");
				} else if (contexts.size() == 1) {
					target.append(genMethodCreateSequenceCall(superConstraints, type, contexts.get(0).getKey()));
					target.newLineIfNotEmpty();
				} else {
					target.append("// error, no contexts. \n");
				}
			}
		};
	}

	/**
	 * Instead of placing large conditional logic directly within each case statement, we now call methods
	 * that encapsulate that logic. See {@link #genConditionMethods()}.
	 */
	private StringConcatenationClient genConditionMethodCall(IConstraint constraint) {
		return inline("condition_", constraint.getName(), "(rule, action)");
	}

	/**
	 * This method generates a separate method that encloses the conditional logic for each case statement.
	 */
	private List<StringConcatenationClient> genConditionMethods() {
		List<StringConcatenationClient> methods = new ArrayList<>();
		for (EPackage pkg : getAccessedPackages()) {
			for (EClass type : getAccessedClasses(pkg)) {
				List<Map.Entry<IConstraint, List<ISerializationContext>>> contexts = getConstraintContexts(type);
				if (contexts.size() > 1) {
					Multimap<EObject, IConstraint> context2constraint = LinkedHashMultimap.create();
					for (Map.Entry<IConstraint, List<ISerializationContext>> entry : contexts) {
						for (ISerializationContext ctx : entry.getValue()) {
							context2constraint.put(((SerializationContext) ctx).getActionOrRule(), entry.getKey());
						}
					}
					for (Map.Entry<IConstraint, List<ISerializationContext>> entry : contexts) {
						IConstraint constraint = entry.getKey();
						List<ISerializationContext> serializationContexts = entry.getValue();
						methods.add(new StringConcatenationClient() {
							@Override
							protected void appendTo(TargetStringConcatenation target) {
								target.append("private boolean condition_");
								target.append(constraint.getName());
								target.append("(");
								target.append(ParserRule.class);
								target.append(" rule, ");
								target.append(Action.class);
								target.append(" action) {\n\treturn (");
								target.append(genCondition(serializationContexts, constraint, context2constraint), "\t");
								target.append(");\n}\n");
							}
						});
					}
				}
			}
		}
		return methods;
	}

	private StringConcatenationClient genCondition(List<ISerializationContext> contexts, IConstraint constraint, Multimap<EObject, IConstraint> ctx2ctr) {
		SetMultimap<EObject, ISerializationContext> index = LinkedHashMultimap.create();
		contexts.stream().sorted().forEach(context -> index.put(getContextObject(context), context));
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				boolean first = true;
				for (EObject obj : index.keySet()) {
					if (!first) {
						target.appendImmediate("\n\t\t|| ", "");
					}
					first = false;
					target.append(genObjectSelector(obj));
					if (ctx2ctr.get(obj).size() > 1) {
						target.append(genParameterSelector(index.get(obj), constraint));
					}
				}
			}
		};
	}

	private StringConcatenationClient genObjectSelector(EObject obj) {
		if (obj instanceof Action) {
			return inline("action == grammarAccess.", grammarAccessExtensions.gaAccessor(obj));
		}
		if (obj instanceof ParserRule) {
			return inline("rule == grammarAccess.", grammarAccessExtensions.gaAccessor(obj));
		}
		return null;
	}

	private StringConcatenationClient genParameterSelector(Set<ISerializationContext> contexts, IConstraint constraint) {
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append(" && (");
				boolean first = true;
				for (ISerializationContext context : contexts) {
					if (!first) {
						target.appendImmediate("\n\t\t\t|| ", " ");
					}
					first = false;
					target.append(genParameterCondition(context, constraint), " ");
				}
				target.append(")");
			}
		};
	}

	private EObject getContextObject(ISerializationContext context) {
		Action assignedAction = context.getAssignedAction();
		return assignedAction != null ? assignedAction : context.getParserRule();
	}

	private StringConcatenationClient genMethodCreateSequenceCall(Map<IConstraint, IConstraint> superConstraints, EClass type, IConstraint key) {
		IConstraint constraint = Objects.requireNonNullElse(superConstraints.get(key), key);
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("sequence_");
				target.append(constraint.getSimpleName());
				target.append("(context, (");
				target.append(type);
				target.append(") semanticObject); \nreturn; \n");
			}
		};
	}

	private StringConcatenationClient genMethodSequenceComment(IConstraint constraint) {
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("""
						// This method is commented out because it has the same signature as another method in this class.
						// This is probably a bug in Xtext's serializer, please report it here:\s
						// https://bugs.eclipse.org/bugs/enter_bug.cgi?product=TMF
						//
						// Contexts:
						""");
				target.append("//     ");
				target.append(joinSortedContexts(constraint).replaceAll("\\n", "\n//     "));
				target.newLineIfNotEmpty();
				target.append("//\n// Constraint:\n//     ");
				if (constraint.getBody() == null) {
					target.append("{");
					target.append(constraint.getType().getName());
					target.append("}");
				} else {
					target.append(constraint.getBody().toString().replaceAll("\\n", "\n//     "));
				}
				target.newLineIfNotEmpty();
				target.append("//\n// protected void sequence_");
				target.append(constraint.getSimpleName());
				target.append("(");
				target.append(ISerializationContext.class);
				target.append(" context, ");
				target.append(constraint.getType());
				target.append(" semanticObject) { }\n");
			}
		};
	}

	private StringConcatenationClient genMethodSequence(IConstraint constraint) {
		ResourceSet resourceSet = getLanguage().getResourceSet();
		StringConcatenationClient cast = GenModelUtil2.getGenClass(constraint.getType(), resourceSet).isEObjectExtension()
				? inline()
				: inline("(", EObject.class, ") ");
		List<ISemState> states = semanticSequencerExtensions.getLinearListOfMandatoryAssignments(constraint);
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("""
						/**
						 * <pre>
						 * Contexts:
						""");
				target.append(" *     ");
				target.append(joinSortedContexts(constraint).replaceAll("\\n", "\n*     "), " ");
				target.newLineIfNotEmpty();
				target.append("""
						 *
						 * Constraint:
						""");
				target.append(" *     ");
				if (constraint.getBody() == null) {
					target.append("{");
					target.append(constraint.getType().getName());
					target.append("}");
				} else {
					String body = constraint.getBody().toString()
							.replaceAll("\\n", "\n*     ")
							.replaceAll("<", "&lt;")
							.replaceAll(">", "&gt;");
					target.append(body, " ");
				}
				target.newLineIfNotEmpty();
				target.append("""
						 * </pre>
						 */
						protected void sequence_""");
				target.append(constraint.getSimpleName());
				target.append("(");
				target.append(ISerializationContext.class);
				target.append(" context, ");
				target.append(constraint.getType());
				target.append(" semanticObject) {\n");
				if (states != null) {
					target.append("\tif (errorAcceptor != null) {\n");
					for (ISemState state : states) {
						EStructuralFeature feature = state.getFeature();
						EPackage featurePackage = feature.getEContainingClass().getEPackage();
						String featureLiteral = GenModelUtil2.getFeatureLiteral(feature, resourceSet);
						target.append("\t\tif (transientValues.isValueTransient(");
						target.append(cast);
						target.append("semanticObject, ");
						target.append(featurePackage);
						target.append(".");
						target.append(featureLiteral);
						target.append(") == ");
						target.append(ValueTransient.class);
						target.append(".YES)\n\t\t\terrorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(");
						target.append(cast);
						target.append("semanticObject, ");
						target.append(featurePackage);
						target.append(".");
						target.append(featureLiteral);
						target.append("));\n");
					}
					target.append("\t}\n\t");
					target.append(SequenceFeeder.class);
					target.append(" feeder = createSequencerFeeder(context, ");
					target.append(cast);
					target.append("semanticObject);\n");
					for (ISemState state : states) {
						target.append("\tfeeder.accept(grammarAccess.");
						target.append(grammarAccessExtensions.gaAccessor(state.getAssignedGrammarElement()));
						target.append(", semanticObject.");
						target.append(getUnresolvingGetAccessor(state.getFeature(), resourceSet));
						target.append(");\n");
					}
					target.append("\tfeeder.finish();\n");
				} else {
					target.append("\tgenericSequencer.createSequence(context, ");
					target.append(cast);
					target.append("semanticObject);\n");
				}
				target.append("}\n\n");
				if (isGenerateSupportForDeprecatedContextEObject()) {
					target.append("@Deprecated\nprotected void sequence_");
					target.append(constraint.getSimpleName());
					target.append("(");
					target.append(EObject.class);
					target.append(" context, ");
					target.append(constraint.getType());
					target.append(" semanticObject) {\n\tsequence_");
					target.append(constraint.getSimpleName());
					target.append("(createContext(context, semanticObject), semanticObject);\n}\n");
				}
			}
		};
	}

	private StringConcatenationClient getUnresolvingGetAccessor(EStructuralFeature feature, ResourceSet resourceSet) {
		GenFeature genFeature = GenModelUtil2.getGenFeature(feature, resourceSet);
		if (genFeature.isResolveProxies()) {
			return inline("eGet(", genFeature.getGenPackage().getEcorePackage(), ".", GenModelUtil2.getFeatureLiteral(genFeature, resourceSet), ", false)");
		} else {
			return inline(GenModelUtil2.getGetAccessor(genFeature, resourceSet), "()");
		}
	}

	private static String joinSortedContexts(IConstraint constraint) {
		return constraint.getContexts().stream()
				.sorted()
				.map(String::valueOf)
				.collect(joining("\n"));
	}

	/**
	 * A single-line template made of the given parts. Like a template expression, a {@code null} part renders nothing.
	 */
	private static StringConcatenationClient inline(Object... parts) {
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				for (Object part : parts) {
					target.append(part);
				}
			}
		};
	}
}
