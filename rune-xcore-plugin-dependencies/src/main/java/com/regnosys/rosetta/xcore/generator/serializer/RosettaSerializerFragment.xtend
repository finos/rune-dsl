package com.regnosys.rosetta.xcore.generator.serializer

import com.google.common.collect.ImmutableSet
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import java.util.List
import java.util.Map
import java.util.Set
import jakarta.inject.Inject
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.Action
import org.eclipse.xtext.Parameter
import org.eclipse.xtext.ParserRule
import org.eclipse.xtext.serializer.ISerializationContext
import org.eclipse.xtext.serializer.acceptor.SequenceFeeder
import org.eclipse.xtext.serializer.analysis.IGrammarConstraintProvider.IConstraint
import org.eclipse.xtext.serializer.analysis.SerializationContext
import org.eclipse.xtext.serializer.sequencer.AbstractDelegatingSemanticSequencer
import org.eclipse.xtext.serializer.sequencer.ITransientValueService
import org.eclipse.xtext.xtext.generator.grammarAccess.GrammarAccessExtensions
import org.eclipse.xtext.xtext.generator.model.FileAccessFactory
import org.eclipse.xtext.xtext.generator.model.annotations.SuppressWarningsAnnotation
import org.eclipse.xtext.xtext.generator.serializer.SemanticSequencerExtensions
import org.eclipse.xtext.xtext.generator.serializer.SerializerFragment2
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static extension org.eclipse.xtext.xtext.generator.model.TypeReference.*
import static extension org.eclipse.xtext.xtext.generator.util.GenModelUtil2.*
import java.util.ArrayList

/**
 * This custom serializer is designed to prevent the semantic sequencer from generating overly large
 * sequence methods in Java, which can cause compiler errors. It achieves this by encapsulating the
 * conditional logic of each case statement into separate methods, which are then invoked within the
 * respective case statements.
 */
class RosettaSerializerFragment extends SerializerFragment2 {
	static val Logger LOG = LoggerFactory.getLogger(RosettaSerializerFragment)
	
	private static def <K, V> Map<K, V> toMap(Iterable<Pair<K, V>> items) {
		val result = newLinkedHashMap
		for (i : items) {
			result.put(i.key, i.value)
		}
		result
	}
	
	@Inject extension SemanticSequencerExtensions
	@Inject extension GrammarAccessExtensions
	@Inject FileAccessFactory fileAccessFactory
	
	override generateAbstractSemanticSequencer() {
		val localConstraints = grammar.grammarConstraints
		val superConstraints = grammar.superGrammar.grammarConstraints
		val newLocalConstraints = localConstraints.filter[type !== null && !superConstraints.contains(it)].toSet
		val clazz = if (isGenerateStub) grammar.abstractSemanticSequencerClass else grammar.semanticSequencerClass
		val superClazz = if (localConstraints.exists[superConstraints.contains(it)]) 
				grammar.usedGrammars.head.semanticSequencerClass
			else
				AbstractDelegatingSemanticSequencer.typeRef
		val javaFile = fileAccessFactory.createGeneratedJavaFile(clazz)
		javaFile.resourceSet = language.resourceSet
		val methodSignatures = newHashSet()
		
		val superConstraintsMap = grammar.superGrammar.grammarConstraints.map[it->it].toMap
		
		javaFile.content = '''
			public «IF isGenerateStub» abstract «ENDIF»class «clazz.simpleName» extends «superClazz» {
			
				@«Inject»
				private «grammar.grammarAccess» grammarAccess;
				
				«genMethodCreateSequence(superConstraintsMap)»
				
				«FOR conditionMethod : genConditionMethods(superConstraintsMap)»
					«conditionMethod»
					
				«ENDFOR»
				
				«FOR c : newLocalConstraints.sort»
					«IF methodSignatures.add(c.simpleName -> c.type)»
						«genMethodSequence(c)»
					«ELSE»
						«LOG.warn("Skipped generating duplicate method in " + clazz.simpleName)»
						«genMethodSequenceComment(c)»
					«ENDIF»
					
				«ENDFOR»
			}
		'''
		javaFile.annotations += new SuppressWarningsAnnotation
		javaFile.writeTo(projectConfig.runtime.srcGen)
	}
	
	private def Iterable<EPackage> getAccessedPackages() {
		grammar.grammarConstraints.filter[type !== null].map[type.EPackage].toSet.sortBy[name]
	}
	
	private def Iterable<EClass> getAccessedClasses(EPackage pkg) {
		grammar.grammarConstraints.map[type].filter[it !== null && EPackage == pkg].toSet.sortBy[name]
	}	

	private def StringConcatenationClient genMethodCreateSequence(Map<IConstraint, IConstraint> superConstraints) {
		'''
			@Override
			public void sequence(«ISerializationContext» context, «EObject» semanticObject) {
				«EPackage» epackage = semanticObject.eClass().getEPackage();
				«ParserRule» rule = context.getParserRule();
				«Action» action = context.getAssignedAction();
				«Set»<«Parameter»> parameters = context.getEnabledBooleanParameters();
				«FOR pkg : accessedPackages.indexed»
					«IF pkg.key > 0»else «ENDIF»if (epackage == «pkg.value».«packageLiteral»)
						switch (semanticObject.eClass().getClassifierID()) {
						«FOR type : pkg.value.accessedClasses»
							case «pkg.value».«type.getIntLiteral(language.resourceSet)»:
								«genMethodCreateSequenceCaseBody(superConstraints, type)»
						«ENDFOR»
						}
				«ENDFOR»
				if (errorAcceptor != null)
					errorAcceptor.accept(diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context));
			}
		'''
	}

	private def StringConcatenationClient genParameterCondition(ISerializationContext context, IConstraint constraint) {
		val values = context.enabledBooleanParameters
		if (!values.isEmpty) {
			'''«ImmutableSet».of(«values.map["grammarAccess."+gaAccessor].join(", ")»).equals(parameters)'''
		} else if (constraint.contexts.exists[!(it as SerializationContext).declaredParameters.isEmpty]) {
			'''parameters.isEmpty()'''
		} else {
			''''''
		}
	}

	private def StringConcatenationClient genMethodCreateSequenceCaseBody(Map<IConstraint, IConstraint> superConstraints, EClass type) {
		val contexts = grammar.getGrammarConstraints(type).entrySet.sortBy[key.name]
			val context2constraint = LinkedHashMultimap.create
			for (e : contexts)
				for (ctx : e.value)
					context2constraint.put((ctx as SerializationContext).actionOrRule, e.key)
		'''
			«IF contexts.size > 1»
				«FOR ctx : contexts.indexed»
					«IF ctx.key > 0»else «ENDIF»if («genConditionMethodCall(ctx.value.key)») {
						«genMethodCreateSequenceCall(superConstraints, type, ctx.value.key)»
					}
				«ENDFOR»
				else break;
			«ELSEIF contexts.size == 1»
				«genMethodCreateSequenceCall(superConstraints, type, contexts.head.key)»
			«ELSE»
				// error, no contexts. 
			«ENDIF»
		'''
	}
	
	/**
	 * Instead of placing large conditional logic directly within each case statement, we now call methods
	 * that encapsulate that logic. See {@link genConditionMethodCall}.
	 */
 	private def StringConcatenationClient genConditionMethodCall(IConstraint constraint) {
		'''condition_«constraint.name»(rule, action)'''
	}
	
	/**
	 * This method generates a separate method that encloses the conditional logic for each case statement.
	 */
	private def StringConcatenationClient[] genConditionMethods(Map<IConstraint, IConstraint> superConstraints)	{
		val ArrayList<StringConcatenationClient> methods = newArrayList
		
		for (pkg : accessedPackages.indexed) {
			for (type : pkg.value.accessedClasses) {
				val contexts = grammar.getGrammarConstraints(type).entrySet.sortBy[key.name]
				val context2constraint = LinkedHashMultimap.create
				for (e : contexts)
					for (ctx : e.value)
						context2constraint.put((ctx as SerializationContext).actionOrRule, e.key)
				
				if (contexts.size > 1) {
					for (ctx : contexts.indexed) {
						val serializationContext = ctx.value.value
						val constraint = ctx.value.key
						
						methods.add('''
							private boolean condition_«constraint.name»(«ParserRule» rule, «Action» action) {
								return («genCondition(serializationContext, constraint, context2constraint)»);
							}
						''')
					}

				}
			}
		}
		
		methods
	}
				
	private def StringConcatenationClient genCondition(List<ISerializationContext> contexts, IConstraint constraint, Multimap<EObject, IConstraint> ctx2ctr) {
		val sorted = contexts.sort
		val index = LinkedHashMultimap.create
		sorted.forEach [
			index.put(contextObject, it)
		]
		'''«FOR obj : index.keySet SEPARATOR "\n\t\t|| "»«obj.genObjectSelector»«IF ctx2ctr.get(obj).size > 1»«obj.genParameterSelector(index.get(obj), constraint)»«ENDIF»«ENDFOR»'''
	}	
	
	private def StringConcatenationClient genObjectSelector(EObject obj) {
		switch obj {
			Action: '''action == grammarAccess.«obj.gaAccessor»'''
			ParserRule: '''rule == grammarAccess.«obj.gaAccessor»'''
		}
	}		
	
	private def StringConcatenationClient genParameterSelector(EObject obj, Set<ISerializationContext> contexts, IConstraint constraint) {
//		val rule = GrammarUtil.containingParserRule(obj)
//		if (rule.parameters.isEmpty || !constraint.contexts.exists[!(it as SerializationContext).declaredParameters.isEmpty]) {
//			return ''''''
//		}
//		// figure out which scenarios are independent from the parameter values
//		val withParamsByRule = LinkedHashMultimap.create
//		contexts.forEach [
//			val param = enabledBooleanParameters.head
//			if (param !== null) {
//				withParamsByRule.put(GrammarUtil.containingParserRule(param), it)
//			}
//		]
//		val copy = newLinkedHashSet
//		copy.addAll(contexts)
//
//		// and remove these
//		withParamsByRule.keySet.forEach [
//			val entries = withParamsByRule.get(it)
//			if (entries.size === (1 << it.parameters.size) - 1) {
//				copy.removeAll(entries)
//			} 
//		]
//		
//		if (copy.isEmpty || copy.exists [ !enabledBooleanParameters.isEmpty ]) {
//			// param configuration doesn't matter
//			return ''''''
//		}
		return ''' && («FOR context : contexts SEPARATOR "\n\t\t\t|| "»«context.genParameterCondition(constraint)»«ENDFOR»)'''		
	}
	
	private def EObject getContextObject(ISerializationContext context) {
		context.assignedAction ?: context.parserRule
	}

	private def StringConcatenationClient genMethodCreateSequenceCall(Map<IConstraint, IConstraint> superConstraints, EClass type, IConstraint key) {
		val superConstraint = superConstraints.get(key)
		val constraint = superConstraint ?: key
		'''
			sequence_«constraint.simpleName»(context, («type») semanticObject); 
			return; 
		'''
	}	
				
	private def StringConcatenationClient genMethodSequenceComment(IConstraint c) '''
		// This method is commented out because it has the same signature as another method in this class.
		// This is probably a bug in Xtext's serializer, please report it here: 
		// https://bugs.eclipse.org/bugs/enter_bug.cgi?product=TMF
		//
		// Contexts:
		//     «c.contexts.sort.join("\n").replaceAll("\\n","\n//     ")»
		//
		// Constraint:
		//     «IF c.body === null»{«c.type.name»}«ELSE»«c.body.toString.replaceAll("\\n","\n//     ")»«ENDIF»
		//
		// protected void sequence_«c.simpleName»(«ISerializationContext» context, «c.type» semanticObject) { }
	'''	
								
	private def StringConcatenationClient genMethodSequence(IConstraint c) {
		val rs = language.resourceSet
		val StringConcatenationClient cast =
			if (c.type.getGenClass(rs).isEObjectExtension)
				''''''
			else
				'''(«EObject») '''
		val states = c.linearListOfMandatoryAssignments
		'''
			/**
			 * <pre>
			 * Contexts:
			 *     «c.contexts.sort.join("\n").replaceAll("\\n","\n*     ")»
			 *
			 * Constraint:
			 *     «IF c.body === null»{«c.type.name»}«ELSE»«c.body.toString.replaceAll("\\n","\n*     ").replaceAll("<","&lt;").replaceAll(">", "&gt;")»«ENDIF»
			 * </pre>
			 */
			protected void sequence_«c.simpleName»(«ISerializationContext» context, «c.type» semanticObject) {
				«IF states !== null»
					if (errorAcceptor != null) {
						«FOR s : states»
							if (transientValues.isValueTransient(«cast»semanticObject, «s.feature.EContainingClass.EPackage».«s.feature.getFeatureLiteral(rs)») == «ITransientValueService.ValueTransient».YES)
								errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(«cast»semanticObject, «s.feature.EContainingClass.EPackage».«s.feature.getFeatureLiteral(rs)»));
						«ENDFOR»
					}
					«SequenceFeeder» feeder = createSequencerFeeder(context, «cast»semanticObject);
					«FOR f: states»
						feeder.accept(grammarAccess.«f.assignedGrammarElement.gaAccessor()», semanticObject.«f.feature.getUnresolvingGetAccessor(rs)»);
					«ENDFOR»
					feeder.finish();
				«ELSE»
					genericSequencer.createSequence(context, «cast»semanticObject);
				«ENDIF»
			}
			
			«IF generateSupportForDeprecatedContextEObject»
				@Deprecated
				protected void sequence_«c.simpleName»(«EObject» context, «c.type» semanticObject) {
					sequence_«c.simpleName»(createContext(context, semanticObject), semanticObject);
				}
			«ENDIF»
		'''
	}
	
	def private StringConcatenationClient getUnresolvingGetAccessor(EStructuralFeature feature, ResourceSet resourceSet) {
		val genFeature = getGenFeature(feature, resourceSet)
		if(genFeature.isResolveProxies) {
			return '''eGet(«genFeature.genPackage.getEcorePackage».«getFeatureLiteral(genFeature, resourceSet)», false)'''
		} else {
			return '''«getGetAccessor(genFeature, resourceSet)»()'''
		}
	}																
}