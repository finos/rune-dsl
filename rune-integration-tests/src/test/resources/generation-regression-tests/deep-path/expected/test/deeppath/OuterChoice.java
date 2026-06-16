package test.deeppath;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneChoiceType;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.BuilderMerger;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import java.util.Objects;
import test.deeppath.meta.OuterChoiceMeta;

import static java.util.Optional.ofNullable;

/**
 * @version 0.0.0
 */
@RosettaDataType(value="OuterChoice", builder=OuterChoice.OuterChoiceBuilderImpl.class, version="0.0.0")
@RuneDataType(value="OuterChoice", model="test", builder=OuterChoice.OuterChoiceBuilderImpl.class, version="0.0.0")
@RuneChoiceType
public interface OuterChoice extends RosettaModelObject {

	OuterChoiceMeta metaData = new OuterChoiceMeta();

	/*********************** Getter Methods  ***********************/
	InnerChoice getInnerChoice();
	Leaf getLeaf();

	/*********************** Build Methods  ***********************/
	OuterChoice build();
	
	OuterChoice.OuterChoiceBuilder toBuilder();
	
	static OuterChoice.OuterChoiceBuilder builder() {
		return new OuterChoice.OuterChoiceBuilderImpl();
	}

	/*********************** Utility Methods  ***********************/
	@Override
	default RosettaMetaData<? extends OuterChoice> metaData() {
		return metaData;
	}
	
	@Override
	@RuneAttribute("@type")
	default Class<? extends OuterChoice> getType() {
		return OuterChoice.class;
	}
	
	@Override
	default void process(RosettaPath path, Processor processor) {
		processRosetta(path.newSubPath("InnerChoice"), processor, InnerChoice.class, getInnerChoice());
		processRosetta(path.newSubPath("Leaf"), processor, Leaf.class, getLeaf());
	}
	

	/*********************** Builder Interface  ***********************/
	interface OuterChoiceBuilder extends OuterChoice, RosettaModelObjectBuilder {
		InnerChoice.InnerChoiceBuilder getOrCreateInnerChoice();
		@Override
		InnerChoice.InnerChoiceBuilder getInnerChoice();
		Leaf.LeafBuilder getOrCreateLeaf();
		@Override
		Leaf.LeafBuilder getLeaf();
		OuterChoice.OuterChoiceBuilder setInnerChoice(InnerChoice _InnerChoice);
		OuterChoice.OuterChoiceBuilder setLeaf(Leaf _Leaf);

		@Override
		default void process(RosettaPath path, BuilderProcessor processor) {
			processRosetta(path.newSubPath("InnerChoice"), processor, InnerChoice.InnerChoiceBuilder.class, getInnerChoice());
			processRosetta(path.newSubPath("Leaf"), processor, Leaf.LeafBuilder.class, getLeaf());
		}
		

		OuterChoice.OuterChoiceBuilder prune();
	}

	/*********************** Immutable Implementation of OuterChoice  ***********************/
	class OuterChoiceImpl implements OuterChoice {
		private final InnerChoice innerChoice;
		private final Leaf leaf;
		
		protected OuterChoiceImpl(OuterChoice.OuterChoiceBuilder builder) {
			this.innerChoice = ofNullable(builder.getInnerChoice()).map(f->f.build()).orElse(null);
			this.leaf = ofNullable(builder.getLeaf()).map(f->f.build()).orElse(null);
		}
		
		@Override
		@RosettaAttribute("InnerChoice")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("InnerChoice")
		public InnerChoice getInnerChoice() {
			return innerChoice;
		}
		
		@Override
		@RosettaAttribute("Leaf")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("Leaf")
		public Leaf getLeaf() {
			return leaf;
		}
		
		@Override
		public OuterChoice build() {
			return this;
		}
		
		@Override
		public OuterChoice.OuterChoiceBuilder toBuilder() {
			OuterChoice.OuterChoiceBuilder builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(OuterChoice.OuterChoiceBuilder builder) {
			ofNullable(getInnerChoice()).ifPresent(builder::setInnerChoice);
			ofNullable(getLeaf()).ifPresent(builder::setLeaf);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			OuterChoice _that = getType().cast(o);
		
			if (!Objects.equals(innerChoice, _that.getInnerChoice())) return false;
			if (!Objects.equals(leaf, _that.getLeaf())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (innerChoice != null ? innerChoice.hashCode() : 0);
			_result = 31 * _result + (leaf != null ? leaf.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "OuterChoice {" +
				"InnerChoice=" + this.innerChoice + ", " +
				"Leaf=" + this.leaf +
			'}';
		}
	}

	/*********************** Builder Implementation of OuterChoice  ***********************/
	class OuterChoiceBuilderImpl implements OuterChoice.OuterChoiceBuilder {
	
		protected InnerChoice.InnerChoiceBuilder innerChoice;
		protected Leaf.LeafBuilder leaf;
		
		@Override
		@RosettaAttribute("InnerChoice")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("InnerChoice")
		public InnerChoice.InnerChoiceBuilder getInnerChoice() {
			return innerChoice;
		}
		
		@Override
		public InnerChoice.InnerChoiceBuilder getOrCreateInnerChoice() {
			InnerChoice.InnerChoiceBuilder result;
			if (innerChoice!=null) {
				result = innerChoice;
			}
			else {
				result = innerChoice = InnerChoice.builder();
			}
			
			return result;
		}
		
		@Override
		@RosettaAttribute("Leaf")
		@Accessor(AccessorType.GETTER)
		@RuneAttribute("Leaf")
		public Leaf.LeafBuilder getLeaf() {
			return leaf;
		}
		
		@Override
		public Leaf.LeafBuilder getOrCreateLeaf() {
			Leaf.LeafBuilder result;
			if (leaf!=null) {
				result = leaf;
			}
			else {
				result = leaf = Leaf.builder();
			}
			
			return result;
		}
		
		@RosettaAttribute("InnerChoice")
		@Accessor(AccessorType.SETTER)
		@RuneAttribute("InnerChoice")
		@Override
		public OuterChoice.OuterChoiceBuilder setInnerChoice(InnerChoice _innerChoice) {
			this.innerChoice = _innerChoice == null ? null : _innerChoice.toBuilder();
			return this;
		}
		
		@RosettaAttribute("Leaf")
		@Accessor(AccessorType.SETTER)
		@RuneAttribute("Leaf")
		@Override
		public OuterChoice.OuterChoiceBuilder setLeaf(Leaf _leaf) {
			this.leaf = _leaf == null ? null : _leaf.toBuilder();
			return this;
		}
		
		@Override
		public OuterChoice build() {
			return new OuterChoice.OuterChoiceImpl(this);
		}
		
		@Override
		public OuterChoice.OuterChoiceBuilder toBuilder() {
			return this;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public OuterChoice.OuterChoiceBuilder prune() {
			if (innerChoice!=null && !innerChoice.prune().hasData()) innerChoice = null;
			if (leaf!=null && !leaf.prune().hasData()) leaf = null;
			return this;
		}
		
		@Override
		public boolean hasData() {
			if (getInnerChoice()!=null && getInnerChoice().hasData()) return true;
			if (getLeaf()!=null && getLeaf().hasData()) return true;
			return false;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public OuterChoice.OuterChoiceBuilder merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
			OuterChoice.OuterChoiceBuilder o = (OuterChoice.OuterChoiceBuilder) other;
			
			merger.mergeRosetta(getInnerChoice(), o.getInnerChoice(), this::setInnerChoice);
			merger.mergeRosetta(getLeaf(), o.getLeaf(), this::setLeaf);
			
			return this;
		}
	
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof RosettaModelObject) || !getType().equals(((RosettaModelObject)o).getType())) return false;
		
			OuterChoice _that = getType().cast(o);
		
			if (!Objects.equals(innerChoice, _that.getInnerChoice())) return false;
			if (!Objects.equals(leaf, _that.getLeaf())) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int _result = 0;
			_result = 31 * _result + (innerChoice != null ? innerChoice.hashCode() : 0);
			_result = 31 * _result + (leaf != null ? leaf.hashCode() : 0);
			return _result;
		}
		
		@Override
		public String toString() {
			return "OuterChoiceBuilder {" +
				"InnerChoice=" + this.innerChoice + ", " +
				"Leaf=" + this.leaf +
			'}';
		}
	}
}
