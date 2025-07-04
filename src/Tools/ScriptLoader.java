package Tools;

import Engine.Core.Console;
import Engine.Core.GameObject;

import javax.tools.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public class ScriptLoader {

    public static Object compileAndLoad(String scriptPath, String className, Class<?>[] paramTypes, Object[] args) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("No compiler. Use a JDK, not a JRE.");

        // Generate isolated output directory for compiled class
        File sourceFile = new File(scriptPath);
        File outputDir = new File("compiled/" + UUID.randomUUID());
        outputDir.mkdirs();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null);

        // Set output directory
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(outputDir));

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(List.of(sourceFile));
        String classpath = buildClasspath("out/production/SypherEngine/", "libs/");
        List<String> options = Arrays.asList("-classpath", classpath);

        boolean success = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();
        fileManager.close();

        if (!success) {
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                System.err.printf("Compile error: %s (line %d)%n", d.getMessage(Locale.getDefault()), d.getLineNumber());
                Console.error(String.format("Compile error: %s (line %d)%n", d.getMessage(Locale.getDefault()), d.getLineNumber()));
            }
            return null;
        }

        File[] jars = new File("libs/").listFiles((dir, name) -> name.endsWith(".jar"));
        List<URL> urls = new ArrayList<>();
        urls.add(outputDir.toURI().toURL());
        if (jars != null) {
            for (File jar : jars) urls.add(jar.toURI().toURL());
        }

        try (URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), ScriptLoader.class.getClassLoader())) {
            Class<?> clazz = classLoader.loadClass(className);
            Constructor<?> constructor = clazz.getConstructor(paramTypes);
            return constructor.newInstance(args);
        }
    }

    public static Class<?> compileAndReturnClass(String scriptPath, String className, String engineClasspath) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("No compiler. Use a JDK, not a JRE.");

        File sourceFile = new File(scriptPath);
        File outputDir = new File("compiled/" + UUID.randomUUID());
        outputDir.mkdirs();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(outputDir));

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFile);

        List<String> options = Arrays.asList("-classpath", engineClasspath);

        boolean success = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();
        fileManager.close();

        if (!success) {
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                System.err.printf("Compile error: %s (line %d)%n", d.getMessage(Locale.getDefault()), d.getLineNumber());
                Console.error("Compile failed: " + d.getMessage(Locale.getDefault()));
            }
            return null;
        }

        URLClassLoader loader = new URLClassLoader(new URL[]{outputDir.toURI().toURL()});
        return loader.loadClass(className);
    }

    public static String buildClasspath(String engineClassesPath, String libsFolderPath) {
        File libsDir = new File(libsFolderPath);
        if (!libsDir.exists() || !libsDir.isDirectory()) {
            throw new RuntimeException("Libs folder not found: " + libsFolderPath);
        }

        String sep = System.getProperty("path.separator"); // ";" on Windows, ":" on Linux/mac

        String jars = Arrays.stream(libsDir.listFiles((dir, name) -> name.endsWith(".jar")))
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(sep));

        // Combine engine classes + jars
        return engineClassesPath + sep + jars;
    }
}
