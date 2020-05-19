package com.regnosys.rosetta.generator.java.object

import java.util.Collection
import java.util.Collections
import java.util.List
import java.util.Map
import java.util.concurrent.atomic.AtomicInteger
import org.eclipse.xtext.generator.IFileSystemAccess2

class NamespaceHierarchyGenerator {

	def generateNamespacePackageHierarchy(IFileSystemAccess2 fsa, 
		Map<String, Collection<String>> namespaceToDescriptionMap, Map<String, Collection<String>> namespaceToModelUriMap) {

		var distinctRoots = namespaceToModelUriMap.keySet.map[it.substring(0, it.indexOf("."))].toSet

		var result = '''
			[«FOR root: distinctRoots SEPARATOR ',' »
			
				«val rootModel = new ModelGroup(root)»
				«namespaceToModelUriMap.keySet
					.filter[it.startsWith(root)]
					.sort
					.forEach[namespace | buildNamespaceModelTree(rootModel, AtomicInteger.newInstance, namespace, namespaceToDescriptionMap, namespaceToModelUriMap)]»
				«buildModelJson(rootModel)»
			
			«ENDFOR»]			
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
		
		if (subNamespaceLength <= 1) {
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
				"name": "«formatName(node.name)»",
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
    protected def String formatName(String name) {
    	if(name.indexOf(".rosetta") > -1) {
	    	 return name.substring(0, name.indexOf(".rosetta"))	
    	} 
    	name;
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
					case !file.startsWith("model") && file.endsWith("-type.rosetta"): "Data"
					case !file.startsWith("model") && file.endsWith("-func.rosetta"): "Func"
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
		protected var String name = null;
		protected var description = null
		protected var uri = null
		protected val List<ModelGroup> children

		new(String name) {
			this(name, null)
		}
		
		new(String name, String description) {
			this.name = name
			this.description = description
			this.children = newArrayList
		}		
		
	}

}
