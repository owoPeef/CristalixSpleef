package ru.peef.spleef;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class DependencyLoader {
    public static void loadDependency(File file) throws Exception {
        URL url = file.toURI().toURL();
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        java.lang.reflect.Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(classLoader, url);
    }
}
