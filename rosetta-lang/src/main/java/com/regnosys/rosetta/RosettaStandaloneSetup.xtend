/*
 * generated by Xtext 2.24.0
 */
package com.regnosys.rosetta

import com.regnosys.rosetta.rosetta.RosettaPackage
import com.regnosys.rosetta.rosetta.simple.SimplePackage
import org.eclipse.emf.ecore.EPackage
import com.regnosys.rosetta.rosetta.expression.ExpressionPackage
import com.google.inject.Injector
import org.eclipse.emf.ecore.EValidator

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
class RosettaStandaloneSetup extends RosettaStandaloneSetupGenerated {

	def static void doSetup() {
		new RosettaStandaloneSetup().createInjectorAndDoEMFRegistration()
	}
	
	override Injector createInjectorAndDoEMFRegistration() {
		EValidator.Registry.INSTANCE.clear // This line is to ensure tests don't use the same validator instance.
		return super.createInjectorAndDoEMFRegistration()
	}

	/**
	 * Register xcore model in standalone setup. 
	 */
	override register(Injector injector) {

		if (!EPackage.Registry.INSTANCE.containsKey(RosettaPackage.eNS_URI)) {
			EPackage.Registry.INSTANCE.put(RosettaPackage.eNS_URI, RosettaPackage.eINSTANCE);
		}
		if (!EPackage.Registry.INSTANCE.containsKey(SimplePackage.eNS_URI)) {
			EPackage.Registry.INSTANCE.put(SimplePackage.eNS_URI, SimplePackage.eINSTANCE);
		}
		if (!EPackage.Registry.INSTANCE.containsKey(ExpressionPackage.eNS_URI)) {
			EPackage.Registry.INSTANCE.put(ExpressionPackage.eNS_URI, ExpressionPackage.eINSTANCE);
		}
		super.register(injector)
	}
}
