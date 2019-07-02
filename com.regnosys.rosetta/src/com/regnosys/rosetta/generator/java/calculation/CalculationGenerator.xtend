package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.RosettaOutputConfigurationProvider
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.object.ModelObjectBoilerPlate
import com.regnosys.rosetta.rosetta.RosettaArgumentFeature
import com.regnosys.rosetta.rosetta.RosettaArguments
import com.regnosys.rosetta.rosetta.RosettaCalculation
import com.regnosys.rosetta.rosetta.RosettaCalculationFeature
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaFeatureOwner
import com.regnosys.rosetta.rosetta.RosettaPackage
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RUnionType
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.rosetta.model.lib.functions.Formula
import com.rosetta.model.lib.functions.ICalculationInput
import com.rosetta.model.lib.functions.ICalculationResult
import com.rosetta.model.lib.functions.IFunctionResult
import com.rosetta.model.lib.functions.IResult
import com.rosetta.model.lib.functions.IResult.Attribute
import java.util.ArrayList
import java.util.Arrays
import java.util.List
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.nodemodel.ICompositeNode
import org.eclipse.xtext.nodemodel.ILeafNode
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*
import com.regnosys.rosetta.rosetta.RosettaExternalFunction

class CalculationGenerator {

	@Inject JavaNames.Factory factory
	@Inject RosettaTypeProvider typeProvider
	@Inject extension RosettaExtensions
	@Inject extension RosettaToJavaExtensions
	@Inject ModelObjectBoilerPlate boilerPlates
	@Inject extension RosettaExternalFunctionDependencyProvider

	@Inject extension ResourceDescriptionsProvider

	def generate(RosettaJavaPackages _packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version) {
		val javaNames = factory.create(_packages)

		fsa.generateFunctions(elements, javaNames, version)
		fsa.generateCalculation(elements, javaNames, version)
	}

	def generateCalculation(IFileSystemAccess2 fsa, List<RosettaRootElement> elements, JavaNames javaNames, String version) {
		// For the top level args based calcs
		val grouped = elements.filter(RosettaArguments).groupBy [
			javaNames.toTargetClassName(it.calculation).firstSegment
		]
		grouped.forEach [ String className, List<RosettaArguments> args |
			val javaFileContents = generateJava(className, args, javaNames, version)
			fsa.generateFile('''«javaNames.packages.calculation.directoryName»/«className».java''', javaFileContents)
		]

		val calculationNames = grouped.values.flatten.map[calculation].map[name].toSet

		// If the WG likes this approach, then the above will be decremented
		val groupedCalculations = elements.filter(RosettaCalculation).filter[!calculationNames.contains(name)]
			.map[arguments].map[it as RosettaArguments].groupBy [
			javaNames.toTargetClassName(it.calculation).firstSegment
		]
		groupedCalculations.forEach[ className, List<RosettaArguments> args |
			val javaFileContents = generateJava(className, args as List<RosettaArguments>, javaNames, version)
			fsa.generateFile('''«javaNames.packages.calculation.directoryName»/«className».java''', javaFileContents)
		]
	}

	def private String generateJava(String className, List<RosettaArguments> args, JavaNames javaNames, String version) {
		val concat = new ImportingStringConcatination()
		val argsIterable = args as Iterable<RosettaArguments>
		concat.append(argsIterable.calculationBody(className, javaNames, version))
		
		val javaFileContents = '''
			package «javaNames.packages.calculation.packageName»;
			
			«FOR imp : concat.imports»
				import «imp»;
			«ENDFOR»
			«FOR imp : concat.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«concat.toString»
		'''
		javaFileContents
	}

	def private calculationBody(Iterable<RosettaArguments> args, String className, extension JavaNames javaNames, String version) {

		val index = args.head?.eResource.resourceDescriptions

		val enum = index.getExportedObjectsByType(RosettaPackage.Literals.ROSETTA_ENUMERATION).filter [
			qualifiedName.lastSegment == className
		].head

		val StringConcatenationClient body = if (enum === null) {
				args.head.createCalculationClass(javaNames, className, false)
			} else {
				val inputType = args.head.commonInputClass
				val inputTypeParamName = inputType?.name?.toFirstLower?:''
				val enumTypeName = packages.model.packageName+'.' + args.head.calculation.name.split('\\.').get(0)
				
				'''
				«emptyJavadocWithVersion(version)»
				public class «className» {
					«createMembers(javaNames, args)»
					«createConstructor(javaNames, className, args)»
					public CalculationResult calculate(«toJavaQualifiedType(inputType)» «inputTypeParamName», «enumTypeName» enumValue) {
						switch (enumValue) {
							«FOR enumVal : args»
								«val enumValClass = toTargetClassName(enumVal.calculation).lastSegment»
								case «enumValClass»:
									return new «enumValClass»(«functionDependencies(enumVal).asArguments.join(', ')»).calculate(«inputTypeParamName»);
							«ENDFOR»
							default:
								throw new IllegalArgumentException("Enum value not implemented: " + enumValue);
						}
					}
					
					«FOR enumVal : args»
						«val enumValClass = toTargetClassName(enumVal.calculation).lastSegment»
						«enumVal.createCalculationClass(javaNames, enumValClass, true)»
					«ENDFOR»
					«javaNames.createResultClass(args.head.calculation, true)»
				}'''
			}
		return body
	}

	def generateFunctions(IFileSystemAccess2 fsa, List<RosettaRootElement> elements, extension JavaNames it, String version) {
		elements.filter(RosettaExternalFunction).filter[!isLibrary].forEach [ function |
			val funcionName = toTargetClassName(function)

			val StringConcatenationClient body = '''
				«emptyJavadocWithVersion(version)»
				public interface «funcionName» {
					
					CalculationResult execute(«FOR param : function.parameters SEPARATOR ', '»«param.type.toJavaQualifiedType» «param.name»«ENDFOR»);
					
					«createResultClass(function, false)»
				}
			'''

			val concat = new ImportingStringConcatination()
			concat.append(body)

			fsa.generateFile('''«packages.functions.directoryName»/«funcionName».java''', '''
				package «packages.functions.packageName»;
				
				«FOR imp : concat.imports»
					import «imp»;
				«ENDFOR»
				
				«concat.toString»
			''')

			val implPath = '''«packages.functions.directoryName»/«funcionName»Impl.java'''
			if (!fsa.isFile(implPath, RosettaOutputConfigurationProvider.SRC_MAIN_JAVA_OUTPUT)) {

				val StringConcatenationClient implClazz = '''
					public class «funcionName»Impl {
						
						public «JavaType.create('''«packages.functions.packageName».«funcionName».CalculationResult''')» execute(«FOR param : function.parameters SEPARATOR ', '»«param.type.toJavaQualifiedType» «param.name»«ENDFOR») {
							throw new UnsupportedOperationException("TODO: auto-generated method stub");
						}
					}
				'''
				val concatImpl = new ImportingStringConcatination()
				concatImpl.append(implClazz)

				fsa.generateFile(implPath, RosettaOutputConfigurationProvider.SRC_MAIN_JAVA_OUTPUT, '''
					package «packages.functions.packageName»;
					
					«FOR imp : concatImpl.imports»
						import «imp»;
					«ENDFOR»
					
					«concatImpl.toString»
				''')
			}
		]
	}

	def private StringConcatenationClient createCalculationClass(RosettaArguments arguments, extension JavaNames it, String className, boolean enumGeneration) {
		val calculation = arguments.calculation
		val inputType = arguments.commonInputClass
		val inputArguments = if(inputType !== null) newArrayList('param' + inputType.name) else newArrayList
		inputArguments += functionDependencies(arguments).asArguments

		'''
			public «IF enumGeneration»static «ENDIF»class «className» {
				«createMembers(arguments)»
				«createConstructor(className, arguments)»
				public CalculationResult calculate(«IF inputType !== null»«inputType.toJavaQualifiedType» param«inputType.name»«ENDIF») {
					CalculationInput input = new CalculationInput().create(«inputArguments.join(', ')»);
«««					// TODO: code generate local variables for fields inside CalculationInput s.t. assignments below can access them as local variables
					CalculationResult result = new CalculationResult(input);
					«FOR feature : calculation.features.filter(RosettaCalculationFeature)»
						result.«feature.getNameOrDefault» = «asignment(feature)»;
					«ENDFOR»
					return result;
				}
				
				«createInputClass(className, arguments)»
				«IF !enumGeneration»
					
					«createResultClass(arguments.calculation, false)»
				«ENDIF»
			}
		'''
	}

	def private StringConcatenationClient createConstructor(extension JavaNames it, String className,
		Iterable<RosettaArguments> arguments) {
		val rosettaFunctions = functionDependencies(arguments)

		if (!rosettaFunctions.empty) {
			'''
				public «className»(«rosettaFunctions.asParameters.join(', ')») {
					«FOR func : rosettaFunctions»
						this.«func.name.toFirstLower» = «func.name.toFirstLower»;
					«ENDFOR»
				}
				
			'''
		}

	}

	def private StringConcatenationClient createConstructor(extension JavaNames it, String className, RosettaArguments arguments) {
		createConstructor(className, newArrayList(arguments))
	}

	def private asParameters(Iterable<RosettaExternalFunction> functions) {
		functions.map[new Parameter(name, name.toFirstLower)]
	}
	
	def private asArguments(Iterable<RosettaExternalFunction> functions) {
		functions.map[name.toFirstLower]
	}

	@Data
	static class Parameter {
		String type
		String name

		override toString() {
			type + ' ' + name
		}
	}
	
	def private StringConcatenationClient createMembers(extension JavaNames it, RosettaArguments arguments) {
		createMembers(newArrayList(arguments))
	}

	def private StringConcatenationClient createMembers(extension JavaNames it, Iterable<RosettaArguments> arguments) {
		'''
			«FOR func : functionDependencies(arguments) BEFORE '\n'»
				private final «toJavaQualifiedType(func as RosettaCallableWithArgs)» «func.name.toFirstLower»;
			«ENDFOR»
			
		'''
	}

	def private StringConcatenationClient createInputClass(extension JavaNames it, String calculationName, RosettaArguments arguments) {
		val inputType = arguments.commonInputClass
		val functionParameters = functionDependencies(arguments).asParameters

		'''
			public static class CalculationInput implements «ICalculationInput» {
«««				// hack for code gen to work, when arguments refer to other arguments
«««				// We can instead declare all fields of CalculationInput as local variables and switch off the need to generate argument references that prepend 'input.'
				private CalculationInput input = this;  // For when arguments need to reference other arguments
				«IF !arguments.features.filter(RosettaArgumentFeature).map[typeProvider.getRType(expression)].filter(RUnionType).empty»
					private final «List»<«ICalculationResult»> calculationResults = new «ArrayList»<>();
				«ENDIF»
				«FOR feature : arguments.features.filter(RosettaArgumentFeature)»
					private «toJava(typeProvider.getRType(feature.expression))» «feature.getNameOrDefault»;
				«ENDFOR»
				
				public CalculationInput create(«IF inputType !== null»«inputType.toJavaQualifiedType» inputParam«IF functionParameters.size > 0», «ENDIF»«ENDIF»«functionParameters.join(', ')») {
					«FOR alias : arguments.aliases»
						«toJava(typeProvider.getRType(alias))» «alias.name»Alias = «toJava(alias.expression)»;
					«ENDFOR»
					«FOR feature : arguments.features.filter(RosettaArgumentFeature)»
						«val exprType = typeProvider.getRType(feature.expression)»
						«IF (exprType instanceof RUnionType) »
							«exprType.converter.toTargetClassName.firstSegment».CalculationResult «toCalculationResultVar(exprType)» = new «exprType.converter.toTargetClassName.firstSegment»(«functionDependencies(feature).asArguments.join(', ')»).calculate(inputParam, «toJava(feature.expression)»);
							this.calculationResults.add(«toCalculationResultVar(exprType)»);
							this.«feature.getNameOrDefault» = «toCalculationResultVar(exprType)».getValue();
						«ELSE»
						this.«feature.getNameOrDefault» = «toJava(feature.expression)»;
						«ENDIF»
					«ENDFOR»
					return this;
				}

				@Override
				public «List»<«Formula»> getFormulas() {
					return «Arrays».asList(«FOR feature : arguments.calculation.features SEPARATOR ','»
						new «Formula»("«calculationName.escape»", "«feature.extractGrammarText.escape»", this)«ENDFOR»);
				}
				
				«IF !arguments.features.filter(RosettaArgumentFeature).map[typeProvider.getRType(expression)].filter(RUnionType).empty»
					@Override
					public «List»<«ICalculationResult»> getCalculationResults() {
						return calculationResults;
					}
					
				«ENDIF»
				«FOR feature : arguments.features.filter(RosettaArgumentFeature)»
					public «toJava(typeProvider.getRType(feature.expression))» get«feature.getNameOrDefault.toFirstUpper»() {
						return «feature.getNameOrDefault»;
					}

				«ENDFOR»
				private static final «List»<«IResult.Attribute»<?>> ATTRIBUTES =  «Arrays».asList(
					«FOR feature : arguments.features.filter(RosettaArgumentFeature) SEPARATOR ','»
						new «Attribute»<>("«feature.getNameOrDefault»", «toJava(typeProvider.getRType(feature.expression))».class, («IResult» res) -> ((CalculationInput) res).get«feature.getNameOrDefault.toFirstUpper»())
					«ENDFOR»
				);
			
				@Override
				public «List»<«Attribute»<?>> getAttributes() {
					return ATTRIBUTES;
				}
				
			}
		'''
	}
	
	protected def CharSequence escape(String s) {
		s.trim.replace('"', "'").replace('\n',' ').replace("\r", "");
	}
	
	
	protected def CharSequence toCalculationResultVar(extension JavaNames it, RUnionType exprType) {
		exprType.converter.toTargetClassName.firstSegment.toFirstLower + "CalculationResult"
	}
		
	
	def private StringConcatenationClient createResultClass(extension JavaNames it, RosettaFeatureOwner featureOwner, boolean enumGeneration) {
		val features = featureOwner.features

		'''
			«IF featureOwner.isCalculation»public static «ENDIF»class CalculationResult implements «featureOwner.resultsClass» {
			
				«IF featureOwner.isCalculation»
					private «calulationInputClass(enumGeneration)» calculationInput;
				«ENDIF»
			
			«FOR feature : features»
				«val nameOrDefault = feature.getNameOrDefault»
					private «feature.toJavaQualifiedType» «nameOrDefault»;
			«ENDFOR»
				
				public CalculationResult(«IF featureOwner.isCalculation»«calulationInputClass(enumGeneration)» calculationInput«ENDIF») {
					«IF featureOwner.isCalculation»this.calculationInput = calculationInput;«ENDIF»
				}
				«FOR feature : features»
					«val nameOrDefault = feature.getNameOrDefault»
					public «feature.toJavaQualifiedType» get«nameOrDefault.toFirstUpper»() {
						return this.«nameOrDefault»;
					}
					
					public CalculationResult set«nameOrDefault.toFirstUpper»(«feature.toJavaQualifiedType» «nameOrDefault») {
						this.«nameOrDefault» = «nameOrDefault»;
						return this;
					}
					
				«ENDFOR»
				«IF featureOwner.isCalculation»
					@Override
					public «calulationInputClass(enumGeneration)» getCalculationInput() {
						return calculationInput;
					}
					
				«ENDIF»
				private static final «List»<«Attribute»<?>> ATTRIBUTES =  «Arrays».asList(
					«FOR feature : features SEPARATOR ','»
						new «Attribute»<>("«feature.getNameOrDefault»", «feature.toJavaQualifiedType».class, («IResult» res) -> ((CalculationResult) res).get«feature.getNameOrDefault.toFirstUpper»())
					«ENDFOR»
				);
			
				@Override
				public «List»<«Attribute»<?>> getAttributes() {
					return ATTRIBUTES;
				}
				
				«boilerPlates.calculationResultBoilerPlate("CalculationResult", featureOwner.features)»
			}
		'''
	}
		
	protected def String extractGrammarText(RosettaFeature rosettaFeature) {	
		val ICompositeNode node = NodeModelUtils.getNode(rosettaFeature);
		if (node === null) {
			return null;
		}
		if (node instanceof ILeafNode) {
			return node.getText();
		} else {
			val StringBuilder builder = new StringBuilder(Math.max(node.getTotalLength(), 1));

			for (ILeafNode leaf : node.getLeafNodes()) {
				builder.append(leaf.getText());
			}
			return builder.toString().trim.replace('\n', '\\n').replace("\r","");
		}
	}
	
	protected def StringConcatenationClient calulationInputClass(boolean enumGeneration)
		'''«IF enumGeneration»«ICalculationInput»«ELSE»CalculationInput«ENDIF»'''
	
	protected def StringConcatenationClient resultsClass(RosettaFeatureOwner featureOwner)
		'''«IF featureOwner.isCalculation»«ICalculationResult»«ELSE»«IFunctionResult»«ENDIF»'''
	
	
	protected def boolean isCalculation(RosettaFeatureOwner featureOwner) {
		!(featureOwner instanceof RosettaExternalFunction)
	}

	def private StringConcatenationClient asignment(extension JavaNames it, RosettaCalculationFeature feature) {
		if (feature.isTypeInferred) {
			toJava(feature.expression)
		} else {
		 val expected = typeProvider.getRType(feature.type)
		 val actual = typeProvider.getRType(feature.expression)
		 
		 if (RBuiltinType.NUMBER == expected && RBuiltinType.NUMBER != actual)
		 	'''new «toJavaQualifiedType(feature.type)»(«toJava(feature.expression)»)'''
		 else if (RBuiltinType.STRING == expected && RBuiltinType.STRING != actual)
		 	'''«toJava(feature.expression)».toString()'''
		 else {
			toJava(feature.expression)
		 	}		
		 }
	}
}
