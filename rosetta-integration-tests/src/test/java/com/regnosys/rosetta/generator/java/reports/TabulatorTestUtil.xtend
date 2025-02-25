package com.regnosys.rosetta.generator.java.reports

import jakarta.inject.Inject
import com.google.inject.Injector
import java.util.Map
import com.rosetta.util.types.JavaClass
import java.util.List
import com.rosetta.model.lib.reports.Tabulator.Field
import static org.junit.jupiter.api.Assertions.*
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.rosetta.model.lib.reports.Tabulator.FieldValue
import com.rosetta.model.lib.reports.Tabulator.FieldValueVisitor
import org.eclipse.xtend2.lib.StringConcatenation
import com.rosetta.model.lib.reports.Tabulator.MultiNestedFieldValue
import com.rosetta.model.lib.reports.Tabulator.NestedFieldValue

class TabulatorTestUtil {
	@Inject Injector injector
	
	def <T> T createInstance(Map<String, Class<?>> classes, JavaClass<?> tabulatorClassRepr) {
		val tabulatorClass = classes.get(tabulatorClassRepr.canonicalName.withDots)
		return injector.getInstance(tabulatorClass) as T
	}
	
	def void assertFieldsEqual(String expected, List<Field> actual) {
		val actualRepr =
		'''
		«FOR field : actual SEPARATOR '\n'»
			«field.fieldStringRepr»
		«ENDFOR»
		'''
		assertEquals(expected, actualRepr)
	}
	private def StringConcatenationClient fieldStringRepr(Field field) {
		'''
		«field.attributeName»«IF field.multi»*«ENDIF»
		«IF field.name != field.attributeName»"«field.name»"«ENDIF»
			«FOR child : field.children SEPARATOR '\n'»
			«child.fieldStringRepr»
			«ENDFOR»
		'''
	}
	def void assertFieldValuesEqual(String expected, List<FieldValue> actual) {
		val actualRepr =
		'''
		«FOR fieldValue : actual SEPARATOR '\n'»
			«fieldValue.fieldValueStringRepr»
		«ENDFOR»
		'''
		assertEquals(expected, actualRepr)
	}
	
	private static def StringConcatenationClient fieldValueStringRepr(FieldValue fieldValue) {
		val visitor = new FieldValueToReprVisitor
		fieldValue.accept(visitor, null)
		'''«visitor.result»'''
	}
	private static class FieldValueToReprVisitor implements FieldValueVisitor<Void> {
		public StringConcatenation result = new StringConcatenation
		override visitMultiNested(MultiNestedFieldValue fieldValue, Void c) {
			result.append(fieldValue.field.attributeName)
			result.append(':')
			if (fieldValue.isPresent) {
				for (vs: fieldValue.value.get) {
					for (v: vs) {
						result.newLine
						result.append("\t")
						result.append(v.fieldValueStringRepr, "\t")
					}
				}
			} else {
				result.append(' <empty>')
			}
		}
		override visitNested(NestedFieldValue fieldValue, Void c) {
			result.append(fieldValue.field.attributeName)
			result.append(':')
			if (fieldValue.isPresent) {
				for (v: fieldValue.value.get) {
					result.newLine
					result.append("\t")
					result.append(v.fieldValueStringRepr, "\t")
				}
			} else {
				result.append(' <empty>')
			}
		}
		override visitSingle(FieldValue fieldValue, Void c) {
			result.append(fieldValue.field.attributeName)
			result.append(': ')
			result.append(fieldValue.value.map[toString].orElse("<empty>"))
		}
	}
}