package com.regnosys.rosetta.generator.java.object

import java.util.Collection
import java.util.Collections
import java.util.List
import java.util.Map
import java.util.concurrent.atomic.AtomicInteger
import org.eclipse.xtext.generator.IFileSystemAccess2

class NamespaceHierarchyGenerator {

	@Deprecated
	// this is a special case and needs to be removed when all the namespaces have been migrated.
	val String ORG_ISDA_CDM_NAMESPACE_ROOT = "org.isda.cdm"
	
	def generateNamespacePackageHierarchy(IFileSystemAccess2 fsa, 
		Map<String, Collection<String>> modelDescriptionMap, Map<String, Collection<String>> modelUriMap) {

		val cdm = new ModelGroup(null, "")
		modelUriMap.keySet
			.filter[it != ORG_ISDA_CDM_NAMESPACE_ROOT]
			.sort
			.forEach[namespace | buildNamespaceModelTree(cdm, AtomicInteger.newInstance, namespace, modelDescriptionMap, modelUriMap)]
		
		val isda = new ModelGroup(ORG_ISDA_CDM_NAMESPACE_ROOT, "")
		buildNamespaceModelTree(isda, AtomicInteger.newInstance, ORG_ISDA_CDM_NAMESPACE_ROOT, modelDescriptionMap, modelUriMap)

		var result = '''
			[«buildModelJson(isda)», «buildModelJson(cdm)»]
			
		'''
		fsa.generateFile('''/namespace-hierarchy.json''', result)
		return result
	}

	protected def ModelGroup buildNamespaceModelTree(ModelGroup node, AtomicInteger namespaceIndex, String namespace,
		Map<String, Collection<String>> modelDescriptionMap, Map<String, Collection<String>> modelUriMap) {
		val String[] namespaceSplit = namespace.split("\\.")
		var subNamespaceLength = namespaceSplit.subList(namespaceIndex.get, namespaceSplit.length).length


		if(node.name === null) {
			node.name = namespaceSplit.get(namespaceIndex.get)
		}
		
		if (subNamespaceLength <= 1 || namespace == ORG_ISDA_CDM_NAMESPACE_ROOT) {
			// add files
			var children = createFileChildrenNodes(namespace, modelDescriptionMap, modelUriMap)
			node.children.addAll(children)
			return node
		} else {
			var found = false;
			namespaceIndex.incrementAndGet;
			for (ModelGroup child : node.children) {
				if (child.name == namespaceSplit.get(namespaceIndex.get)) {
					found = true
					buildNamespaceModelTree(child, namespaceIndex, namespace, modelDescriptionMap, modelUriMap)
				}
			}

			if (!found) {
				var description = extractNamespaceDescription(namespaceSplit, namespaceIndex, modelDescriptionMap)
				var child = new ModelGroup(namespaceSplit.get(namespaceIndex.get), description)
				node.children.add(child)
				buildNamespaceModelTree(child, namespaceIndex, namespace, modelDescriptionMap, modelUriMap)
			}
		}

		node
	}

	protected def String buildModelJson(ModelGroup node) {
		'''
			{
				"name": "«node.name»",
				"description": "«node.description»"«IF node.uri !== null»,
				"uri": "«node.uri»"«ENDIF»«IF node.children.length > 0», 
					"children": [
						«FOR child : node.children SEPARATOR ','»
							«buildModelJson(child)» 
						«ENDFOR» 
					]
				«ENDIF»
				
			}
		'''
	}

	protected def List<ModelGroup> createFileChildrenNodes(String namespace,
		Map<String, Collection<String>> modelDescriptionMap, Map<String, Collection<String>> modelUriMap) {
		val listOfFiles = modelUriMap.getOrDefault(namespace, Collections.EMPTY_LIST)
		val files = newArrayList
		if (listOfFiles.length > 0) {
			listOfFiles.forEach [ file |
				var name = switch file {
					// old file name to remain, but new should change
					case !file.startsWith("model") && file.endsWith("-enum.rosetta"): "Enum"
					case !file.startsWith("model") && file.endsWith("-type.rosetta"): "Type"
					case !file.startsWith("model") && file.endsWith("-func.rosetta"): "Function"
					default: file
				}
				val descriptions = modelDescriptionMap.getOrDefault(namespace, Collections.EMPTY_LIST)
				var fileNode = new ModelGroup(name, descriptions.join(" "))
				fileNode.uri = file
				files.add(fileNode)
			]
		}

		files
	}

	protected def String extractNamespaceDescription(String[] namespaceSplit, AtomicInteger namespaceIndex,
		Map<String, Collection<String>> modelDescriptionMap) {
		val intermediateryNamespace = namespaceSplit.subList(0, namespaceIndex.get + 1).join(".")
		val descriptions = modelDescriptionMap.getOrDefault(intermediateryNamespace, Collections.EMPTY_LIST)

		descriptions.join(" ")
	}

	static class ModelGroup {
		protected var name = null;
		protected var description = null
		protected var uri = null
		protected val List<ModelGroup> children

		new(String name, String description) {
			this.name = name
			this.description = description
			this.children = newArrayList
		}
	}

}
