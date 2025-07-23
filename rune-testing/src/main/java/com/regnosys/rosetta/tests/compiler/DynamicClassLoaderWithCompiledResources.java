package com.regnosys.rosetta.tests.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicClassLoaderWithCompiledResources extends ClassLoader {

	private final ClassLoader parent;
	private final Map<String, CompiledCode> customCompiledCode = new HashMap<>();

	public DynamicClassLoaderWithCompiledResources(ClassLoader parent) {
		super(parent);
		this.parent = parent;
	}

	public void addCode(CompiledCode cc) {
		customCompiledCode.put(cc.getName(), cc);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		CompiledCode cc = customCompiledCode.get(name);
		if (cc == null) {
			return super.findClass(name);
		}
		byte[] byteCode = cc.getByteCode();
		return defineClass(name, byteCode, 0, byteCode.length);
	}

	public List<CompiledCode> getCompiledCode(String packageName) {
		List<CompiledCode> result = new ArrayList<>();
		if (parent instanceof DynamicClassLoaderWithCompiledResources) {
			result.addAll(((DynamicClassLoaderWithCompiledResources) parent).getCompiledCode(packageName));
		}
		result.addAll(customCompiledCode.entrySet().stream().filter(e -> e.getKey().startsWith(packageName)).map(e -> e.getValue()).collect(Collectors.toList()));
		return result;
	}
}
