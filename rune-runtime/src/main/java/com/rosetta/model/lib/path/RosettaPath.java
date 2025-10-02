/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rosetta.model.lib.path;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.skip;
import static java.lang.String.format;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

public class RosettaPath implements Comparable<RosettaPath> {
	
    private final RosettaPath parent;
    private final Element element;
    
    //a Path that has 0 notional elements so when you create a subPath from it you get a path with 1 element
    public static class NullPath extends RosettaPath {
    	public NullPath() {
    		super (null, null);
    	}
    	@Override
    	public RosettaPath newSubPath (String p) {
    		return createPath(Element.valueOf(p));
    	}
    }

    private RosettaPath(RosettaPath parent, Element element) {
        this.parent = parent;
        this.element = element;
    }

    public static RosettaPath createPath(RosettaPath parent, Element element) {
        return new RosettaPath(parent, element);
    }

    public static RosettaPath createPath(Element element) {
        return new RosettaPath(null, element);
    }

    public static RosettaPath createPathFromElements(List<Element> elements) {
    	RosettaPath newPath = null;

        for (Element element : elements) {
            if (newPath == null) {
                newPath = createPath(Element.create(element.uri, element.path, element.index, element.attrs));
            } else {
                newPath = newPath.newSubPath(element);
            }
        }
        return newPath;
    }

    public static RosettaPath valueOf(String stringPath) {
        Iterable<String> pathSections = Splitter.on('.').split(stringPath);
        if (size(pathSections) == 0) {
            throw new IllegalArgumentException(stringPath + " is not a valid rosetta path");
        }

        RosettaPath path = RosettaPath.createPath(Element.valueOf(getFirst(pathSections, null)));
        for (String section : skip(pathSections, 1)) {
            path = path.newSubPath(Element.valueOf(section));
        }
        return path;
    }

    public RosettaPath newSubPath(Element element) {
        if (parent == null && element == null) {
            return new RosettaPath(null, null);
        } else {
            return new RosettaPath(this, element);
        }
    }

    public RosettaPath newSubPath(String path, int index) {
    	return newSubPath(Element.create(path, OptionalInt.of(index), null));
    }
  
    public RosettaPath newSubPath(String path) {
    	return newSubPath(Element.create(path, OptionalInt.empty(), null));
    }
    
    public RosettaPath withIndex(int index) {
    	Element el = getElement().withIndex(index);
    	return new RosettaPath(parent, el);
    }
    
    public RosettaPath trimFirst() {
        LinkedList<Element> elements = allElements();
        elements.removeFirst();
        return createPathFromElements(elements);
    }

    public LinkedList<Element> allElements() {
        LinkedList<Element> elements = new LinkedList<>();
        if (hasParent()) {
            elements.addAll(parent.allElements());
        }
        elements.add(element);

        return elements;
    }

    public List<String> allElementPaths() {
        return allElements().stream()
                .map(Element::getPath)
                .collect(Collectors.toList());
    }

    public String buildPath() {
        if (hasParent()) {
            return getParent().buildPath() + "." + element.asPathString();
        }
        return element.asPathString();
    }

    public RosettaPath getParent() {
        return parent;
    }

    public Element getElement() {
        return element;
    }

    private boolean hasParent() {
        return !Objects.isNull(parent);
    }

    public boolean endsWith(RosettaPath other) {
        if (other==null) return true;
        if (!other.element.path.equals("*") && !other.element.path.equals(element.path)) return false;
        if (other.hasParent() && !this.hasParent()) return false;
        if (!this.hasParent() && !other.hasParent()) return true;
        return this.parent.endsWith(other.parent);
    }
    
    public boolean containsPath(RosettaPath subPath) {
    	if (subPath==null) return true;
    	if (subPath.element.path.equals(element.path) || subPath.element.path.equals("*")) {
    		return this.parent.endsWith(subPath.parent);
    	}
    	if (parent==null) return false;
    	return parent.containsPath(subPath);
    }

    public boolean startsWith(RosettaPath other) {
        LinkedList<RosettaPath.Element> list = this.allElements();
        LinkedList<RosettaPath.Element> prefix = other.allElements();
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        if (list == null || list.size() < prefix.size()) {
            return false;
        }

        for (int i = 0; i < prefix.size(); i++) {
            if (!list.get(i).equals(prefix.get(i))) {
                return false;
            }
        }

        return true;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RosettaPath that = (RosettaPath) o;

        if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;
        return element != null ? element.equals(that.element) : that.element == null;
    }

	public boolean matchesIgnoringIndex(RosettaPath path) {
		if (!this.element.path.equals(path.element.path)) return false;
		if (parent==null) return path.parent==null;
		if (path.parent==null) return false;
		return parent.matchesIgnoringIndex(path.parent);
	}
	
	public RosettaPath toIndexless() {
    	RosettaPath newParent = parent==null?null:parent.toIndexless();
    	Element newElement = new Element(element.getUri(), element.getPath(), OptionalInt.empty(), element.attrs);
    	
    	return new RosettaPath(newParent, newElement);
    }

    @Override
    public String toString() {
        return buildPath();
    }

    @Override
    public int hashCode() {
        int result = parent != null ? parent.hashCode() : 0;
        result = 31 * result + (element != null ? element.hashCode() : 0);
        return result;
    }
    
    /**
     * Path elements with a zero index are equivalent to elements with no index. 
     * The compareTo implementation is required to sort paths where no index and zero index are equal.
     */
	@Override
	public int compareTo(RosettaPath other) {
		Iterator<Element> i1 = allElements().iterator();
		Iterator<Element> i2 = other.allElements().iterator();

		while (i1.hasNext() || i2.hasNext()) {
			if (!i1.hasNext())
				return -1;
			if (!i2.hasNext())
				return 1;
			int result = i1.next().compareTo(i2.next());
		    if (result != 0) {
		    	return result;
		    }
		}
		return 0;
	}

    public static class Element implements Comparable<Element> {
        public static final String DEFAULT_URI = "FpML_5_10";

        private final String uri;
        private final String path;
        private final OptionalInt index;
        private final Map<String, String> attrs;

        private Element(String uri, String path, OptionalInt index, Map<String, String> attrs) {
            this.uri = uri;
            this.path = path;
            this.index = index;
            this.attrs = attrs == null ? Collections.emptyMap() : attrs;
        }

		public static Element create(String path, Map<String, String> attrs) {
            return new Element(DEFAULT_URI, path, OptionalInt.empty(), attrs);
        }

        public static Element create(String path, OptionalInt index, Map<String, String> attrs) {
            return new Element(DEFAULT_URI, path, index, attrs);
        }

        public static Element create(String uri, String path, OptionalInt index, Map<String, String> attrs) {
            return new Element(uri, path, index, attrs);
        }

        /**
         * @param element of the form: fieldName(index)[attributes]=value, where (index) and [attributes] are optional
         */
        public static Element valueOf(String element) {
            Pattern p = Pattern.compile("^(\\w+|\\*)(?:\\(([0-9]+)\\))?(?:\\[([\\w\\-]+)=([\\w\\-]+)])?$");
            Matcher matcher = p.matcher(element);

            if (!matcher.matches()) {
                throw new IllegalArgumentException(element + " is not a valid path element");
            }
            OptionalInt index = matcher.group(2) == null ? OptionalInt.empty() : OptionalInt.of(Integer.parseInt(matcher.group(2)));
            String attrKey = matcher.group(3);
            String attrValue = matcher.group(4);
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            if (attrKey != null && attrValue != null) {
                builder.put(attrKey, attrValue);
            }
            return create(matcher.group(1), index, builder.build());
        }

        public Element withIndex(int newIndex) {
			return new Element(uri, path, OptionalInt.of(newIndex), attrs);
		}

        public String getUri() {
            return uri;
        }

        public String getPath() {
            return path;
        }

        public OptionalInt getIndex() {
            return index;
        }

        public Map<String, String> getMetas() {
            return attrs;
        }
        
        private String asPathString() {
            String idSection = !attrs.isEmpty() ? attrs.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(",", "[", "]")) : "";
            String indexSection = index.isPresent() ? format("(%s)", index.getAsInt()) : "";
            return format("%s%s%s", path, indexSection, idSection);
        }

        @Override
        public String toString() {
            return "Element{" +
                    "path='" + path + '\'' +
                    ", index=" + index +
                    ", uri='" + uri + '\'' +
                    ", attrs=" + attrs +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Element element = (Element) o;

            if (uri != null ? !uri.equals(element.uri) : element.uri != null) return false;
            if (path != null ? !path.equals(element.path) : element.path != null) return false;
            if (index != null ? index.orElse(0) != element.index.orElse(0) : element.index != null) return false;
            return attrs != null ? attrs.equals(element.attrs) : element.attrs == null;
        }

        @Override
        public int hashCode() {
            int result = uri != null ? uri.hashCode() : 0;
            result = 31 * result + (path != null ? path.hashCode() : 0);
            result = 31 * result + (index != null ? Integer.hashCode(index.orElse(0)) : 0);
            result = 31 * result + (attrs != null ? attrs.hashCode() : 0);
            return result;
        }

        /**
         * Path elements with a zero index are equivalent to elements with no index. 
         * The compareTo implementation is required to sort paths where no index and zero index are equal.
         */
		@Override
		public int compareTo(Element other) {
			return Comparator
					  .comparing(Element::getPath)
					  .thenComparing((e1, e2) -> Integer.compare(e1.getIndex().orElse(0), e2.getIndex().orElse(0)))
					  .compare(this, other);
		}
    }
    
    static public class RosettaPathTree {
    	private final RosettaPathTree parent;
    	private Object value;
    	private final Map<Element, RosettaPathTree> children = new HashMap<>();    	
    	
    	public RosettaPathTree(RosettaPathTree parent) {
			super();
			this.parent = parent;
		}

		public static RosettaPathTree treeify(Map<RosettaPath, Object> paths) {
    		RosettaPathTree result = new RosettaPathTree(null);
    		for (Entry<RosettaPath, Object> path:paths.entrySet()) {
    			RosettaPathTree pathTree = addToTree(path.getKey(), result);
    			pathTree.value = path.getValue();
    		}
    		return result;
    	}

		private static RosettaPathTree addToTree(RosettaPath path, RosettaPathTree top) {
			if (!path.hasParent()) {
				return top.children.computeIfAbsent(path.element, e->new RosettaPathTree(top));
			}
			else {
				RosettaPathTree parent = addToTree(path.parent, top);
				return parent.children.computeIfAbsent(path.element, e->new RosettaPathTree(parent));
			}
		}
		
		public RosettaPathTree getParent() {
			return parent;
		}

		public Map<Element, RosettaPathTree> getChildren() {
			return children;
		}

		public Object getValue() {
			return value;
		}

		public Object matches(RosettaPath p) {
			RosettaPathTree match = match(p);
			return match==null?null:match.value;
		}
		
		private RosettaPathTree match(RosettaPath p) {
			if (!p.hasParent()) {
				return children.get(p.element);
			}
			else {
				RosettaPathTree t = match(p.parent);
				if (t==null) return null;
				return t.children.get(p.element);
			}
		}
		
		public List<RosettaPathTree> matches(RosettaPath p, Comparator<Element> comparator) {
			return match(p, comparator).collect(Collectors.toList());
		}
		
		private Stream<RosettaPathTree> match(RosettaPath p, Comparator<Element> comparator) {
			if (!p.hasParent()) {
				return children.entrySet().stream().filter(c->comparator.compare(p.element, c.getKey())==0)
						.map(e->e.getValue());
			}
			else {
				Stream<RosettaPathTree> t = match(p.parent, comparator);
				return t.flatMap(q->q.children.entrySet().stream()).filter(c->comparator.compare(p.element, c.getKey())==0)
						.map(e->e.getValue());
			}
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			if (parent!=null) {
				result.append(parent.parentString(this));
				result.append(", ");
			}
			if (children!=null && !children.isEmpty()) {
				result.append("["+children.entrySet().stream().map(e->e.getKey().asPathString()+"="+e.getValue().toString()).collect(Collectors.joining(", "))+"]");
			}
			if (value!=null) {
				result.append("=");
				result.append(value);
			}
			return result.toString();
		}
		
		public String parentString(RosettaPathTree child) {
			StringBuilder result = new StringBuilder();
			if (parent!=null) {
				result.append(parent.parentString(this));
				result.append(", ");
			}
			children.entrySet().stream().filter(e->e.getValue()==child).forEach(e->result.append(e.getKey().asPathString()));
			return result.toString();
		}
    }
}
