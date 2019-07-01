package com.regnosys.rosetta.generator.daml.object

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class DamlModelObjectGeneratorTest {

	@Inject extension CodeGeneratorTestHelper

	@Test
	def void shouldGenerateClassWithImports() {
		val classes = '''
			class Foo
			{
			    stringAttr string (0..1);
			}
		'''.generateDaml.get("Classes")
		
		assertTrue(classes.contains('''import Org.Isda.Cdm.Enums'''))
		assertTrue(classes.contains('''import Org.Isda.Cdm.ZonedDateTime'''))
		assertTrue(classes.contains('''import Org.Isda.Cdm.MetaClasses'''))
		assertTrue(classes.contains('''import Prelude hiding (Party, exercise, id, product, agreement)'''))
	}

	@Test
	def void shouldGenerateClassWithBasicTypes() {
		val classes = '''
			class Foo
			{
			    stringAttr string (1..1);
			    intAttr int (1..1);
			    numberAttr number (1..1);
			    booleanAttr boolean (1..1);
			    dateAttr date (1..1);
			    timeAttr time (1..1);
				zonedDateTimeAttr zonedDateTime (1..1);
			}
		'''.generateDaml.get("Classes")

		assertTrue(classes.contains('''
		data Foo = Foo with 
		  booleanAttr : Bool
		  dateAttr : Date
		  intAttr : Int
		  numberAttr : Decimal
		  stringAttr : Text
		  timeAttr : Text
		  zonedDateTimeAttr : ZonedDateTime
		    deriving (Eq, Ord, Show)'''))
	}

	@Test
	def void shouldGenerateClassWithOptionalBasicType() {
		val classes = '''
			class Foo
			{
			    stringAttr string (0..1);
			}
		'''.generateDaml.get("Classes")
		
		assertTrue(classes.contains('''
		data Foo = Foo with 
		  stringAttr : Optional Text
		    deriving (Eq, Ord, Show)'''))
	}

	@Test
	def void shouldGenerateClassWithComments() {
		val classes = '''
			class Foo <"This is the class comment which should wrap if the line is long enough.">
			{
			    stringAttr string (0..1) <"This is the attribute comment which should also wrap if long enough">;
			}
		'''.generateDaml.get("Classes")
		
		assertTrue(classes.contains('''
		-- | This is the class comment which should wrap if the
		--   line is long enough.
		data Foo = Foo with 
		  stringAttr : Optional Text
		    -- ^ This is the attribute comment which should also wrap
		    --   if long enough
		    deriving (Eq, Ord, Show)'''))
	}

	@Test
	def void shouldGenerateClassWithBasicTypeList() {
		val classes = '''
			class Foo
			{
			    stringAttrs string (0..*);
			}
		'''.generateDaml.get("Classes")
		
		assertTrue(classes.contains('''
		data Foo = Foo with 
		  stringAttrs : [Text]
		    deriving (Eq, Ord, Show)'''))
	}
	
	@Test
	def void shouldGenerateClassWithBasicTypeAndMetaFieldScheme() {
		val code = '''
			metaType scheme string
			
			class Foo
			{
			    stringAttr string (1..1) scheme;
			}
		'''.generateDaml
		
		val classes = code.get("Classes")
		
		assertTrue(classes.contains('''
		data Foo = Foo with 
		  stringAttr : (FieldWithMeta Text)
		    deriving (Eq, Ord, Show)'''))

		val metaFields = code.get("MetaFields")
		
		assertTrue(metaFields.contains('''
		daml 1.2
		
		-- | This file is auto-generated from the ISDA Common
		--   Domain Model, do not edit.
		--   @version test
		module Org.Isda.Cdm.MetaFields
		  ( module Org.Isda.Cdm.MetaFields ) where
		
		data MetaFields = MetaFields with
		  scheme : Optional Text
		    deriving (Eq, Ord, Show)'''))
	}
	
	@Test
	def void shouldGenerateClassWithOptionalRosettaType() {
		val classes = '''
			class Foo
			{
			    barAttr Bar (0..1);
			}
			
			class Bar
			{
			    stringAttr string (1..1);
			}
		'''.generateDaml.get("Classes")
		
		assertTrue(classes.contains('''
		data Foo = Foo with 
		  barAttr : Optional Bar
		    deriving (Eq, Ord, Show)'''))
	}
	
	@Test
	def void shouldGenerateClassWithRosettaTypeAndMetaReference() {
		val code = '''
			metaType reference string
			
			class Foo
			{
			    barReference Bar (0..1) reference;
			}
			
			class Bar key
			{
			    stringAttr string (1..1);
			}
		'''.generateDaml
		
		val classes = code.get("Classes")
		
		assertTrue(classes.contains('''
		data Bar = Bar with 
		  meta : Optional MetaFields
		  stringAttr : Text
		    deriving (Eq, Ord, Show)'''))

		assertTrue(classes.contains('''
		data Foo = Foo with 
		  barReference : Optional (ReferenceWithMeta Bar)
		    deriving (Eq, Ord, Show)'''))

		val metaFields = code.get("MetaFields")
		
		assertTrue(metaFields.contains('''
		daml 1.2
		
		-- | This file is auto-generated from the ISDA Common
		--   Domain Model, do not edit.
		--   @version test
		module Org.Isda.Cdm.MetaFields
		  ( module Org.Isda.Cdm.MetaFields ) where
		
		data MetaFields = MetaFields with
		  reference : Optional Text
		    deriving (Eq, Ord, Show)'''))
	}
	
	@Test
	def void shouldGenerateClassWithRosettaTypeAndMetaBasicReference() {
		val code = '''
			metaType reference string
			
			class Foo
			{
			    stringReference string (0..1) reference;
			}
		'''.generateDaml
		
		val classes = code.get("Classes")
		
		assertTrue(classes.contains('''
		data Foo = Foo with 
		  stringReference : Optional (BasicReferenceWithMeta Text)
		    deriving (Eq, Ord, Show)'''))

		val metaFields = code.get("MetaFields")
		
		// println(metaFields)
		
		assertTrue(metaFields.contains('''
		daml 1.2
		
		-- | This file is auto-generated from the ISDA Common
		--   Domain Model, do not edit.
		--   @version test
		module Org.Isda.Cdm.MetaFields
		  ( module Org.Isda.Cdm.MetaFields ) where
		
		data MetaFields = MetaFields with
		  reference : Optional Text
		    deriving (Eq, Ord, Show)'''))
	}
}