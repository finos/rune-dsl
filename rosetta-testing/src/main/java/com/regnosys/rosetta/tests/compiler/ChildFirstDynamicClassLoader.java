package com.regnosys.rosetta.tests.compiler;

import org.mdkt.compiler.DynamicClassLoader;

// Based on https://stackoverflow.com/a/6424879/3083982
public class ChildFirstDynamicClassLoader extends DynamicClassLoader {

    public ChildFirstDynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                // checking local
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                // checking parent
                // This call to loadClass may eventually call findClass again, in case the parent doesn't find anything.
                c = super.loadClass(name, resolve);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
}
