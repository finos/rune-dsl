package com.regnosys.rosetta.ide.formatting

import org.junit.jupiter.api.Test
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest
import org.eclipse.lsp4j.FormattingOptions
import com.regnosys.rosetta.formatting2.FormattingOptionsAdaptor

class FormattingTest extends AbstractRosettaLanguageServerTest {
	@Test
	def testFormattingWithSmallMaxLineWidth() {
		val model = '''
		namespace foo.bar
		
		type Foo:
			a int (1..1)
		
		func Foo:
			inputs: foo Foo (1..1)
			output: result int (1..1)
			
			set result:
				foo -> a
		'''
		testFormatting(
			[
				val options = new FormattingOptions
				options.putNumber(FormattingOptionsAdaptor.PREFERENCE_MAX_LINE_WIDTH_KEY, 10)
				it.options = options
			],
			[
				it.model = model
				it.expectedText = '''
				namespace foo.bar
				
				type Foo:
					a int (1..1)
				
				func Foo:
					inputs:
						foo Foo (1..1)
					output:
						result int (1..1)
				
					set result:
						foo -> a
				'''
			]
		)
	}
	
	@Test
	def testFormattingWithSmallConditionalMaxLineWidth() {
		val model = '''
		namespace foo.bar
		
		type Foo:
			a int (1..1)
		
		func Foo:
			inputs: foo Foo (1..1)
			output: result int (0..*)
			
			add result:
				foo -> a
			add result:
				if True then 42 else 10
		'''
		testFormatting(
			[
				val options = new FormattingOptions
				options.putNumber(FormattingOptionsAdaptor.PREFERENCE_CONDITIONAL_MAX_LINE_WIDTH_KEY, 10)
				it.options = options
			],
			[
				it.model = model
				it.expectedText = '''
				namespace foo.bar
				
				type Foo:
					a int (1..1)
				
				func Foo:
					inputs:
						foo Foo (1..1)
					output:
						result int (0..*)
				
					add result: foo -> a
					add result:
						if True
						then 42
						else 10
				'''
			]
		)
	}
}