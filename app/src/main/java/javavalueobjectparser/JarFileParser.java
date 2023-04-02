package javavalueobjectparser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class JarFileParser {

    private final static String PACKAGE_NAME_PREFIX = "com.piss.rest.model";
    
    private final JarFile jarFile;
    private final URLClassLoader loader;
    private final List<String> classesPath;
    private final List<Class<?>> classesObj;
    private final List<Map<String,Object>> classesMap;

    public JarFileParser(File file) {
        this.jarFile = getJar(file);
        this.loader = getLoader(file);
        this.classesPath = getClassPath();
        this.classesObj = getClasses();
        this.classesMap = this.classesObj.stream().map(c -> getFieldList(c)).collect(Collectors.toList());
    }

    private JarFile getJar(File file) {
        try {
            return new JarFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Could not create JarFile");
    }

    private URLClassLoader getLoader(File file) {
        try {
            URL jarUrl = new URL(String.format("jar:%s!/", file.toURI().toURL()));
            return new URLClassLoader(new URL[]{jarUrl});
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Could not create URLClassLoader");
    }
    
    private List<String> getClassPath() {
        return this.jarFile.stream()
                .map(JarEntry::getName)
                .filter(s -> s.replace("/", ".").startsWith(PACKAGE_NAME_PREFIX))
                .filter(s -> s.endsWith(".class"))
                .map(s -> s.replace('/', '.').replace(".class", ""))
                .collect(Collectors.toList());
    }

    private List<Class<?>> getClasses() {
        return this.classesPath.stream()
            .map(p -> {
                try {
                    return this.loader.loadClass(p);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            })
            .collect(Collectors.toList());
    }

    private Map<String, Object> getFieldList(Class<?> clz) {
        String key = clz.getSimpleName();
        Map<String, Object> value = Arrays.stream(clz.getDeclaredFields())
            .collect(Collectors.toMap(Field::getName, field -> {
                if (field.getGenericType() instanceof ParameterizedType)
                    return ((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]).getSimpleName();
                return "";
            }));
        return Map.of(key, value);
    } 

    public String getClassesJsonArray() {
        return new Gson().toJson(this.classesMap);
    }

}
