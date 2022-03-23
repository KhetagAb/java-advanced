package info.kgeorgiy.ja.dzestelov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class Implementor implements JarImpler {

    private static final String TAB = "    ";
    private static final String FILE_NAME_SUFFIX = "Impl";
    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    private static final Predicate<Method> IS_ABSTRACT = x -> Modifier.isAbstract(x.getModifiers());

    private int section = 0;

    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + FILE_NAME_SUFFIX;
    }

    private static Path getFileName(Class<?> token, String end) {
        return Path.of(getClassName(token) + end);
    }

    private static Path getFile(Class<?> token, String end) {
        return Path.of(token.getPackageName().replace('.', File.separatorChar)).resolve(getFileName(token, end));
    }


    public static void main(String[] args) {
        if (args == null || args.length < 2 || ("jar".equals(args[0]) && args.length < 3)) {
            System.out.println("Usage: [-jar] className root [outputJarFileName]");
            return;
        } else if (args[0] == null || args[1] == null || (args[0].equals("-jar") && args[2] == null)) {
            System.out.println("Arguments must not be null");
            return;
        }

        int isJar = args[0].equals("-jar") ? 0 : 1;
        JarImpler jarImpler = new Implementor();
        try {
            Class<?> clazz = Class.forName(args[isJar]);
            Path path = Path.of(args[isJar + 1]);
            if (isJar == 1) {
                jarImpler.implementJar(clazz, path);
            } else {
                jarImpler.implement(clazz, path);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Cannot find class with name: " + args[isJar]);
        } catch (InvalidPathException e) {
            System.out.println("Invalid path: " + args[isJar + 1]);
        } catch (ImplerException e) {
            System.out.println("ImplerException occurred: " + e.getMessage());
        }
    }

    private static void checkImplementable(Class<?> token, Path root) throws ImplerException {
        if (root == null) {
            throw new ImplerException("Root must be not null");
        }
        if (token == null || Modifier.isFinal(token.getModifiers()) || token.isPrimitive() || token.equals(Enum.class) || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Cannot implement " + token);
        }
    }

    private static int compileFile(Class<?> token, Path root) throws ImplerException {
        try {
            final String classpath = root + File.pathSeparator + Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
            final String[] args = new String[]{root.resolve(getFile(token, ".java")).toString(), "-cp", classpath};
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            return compiler.run(null, null, null, args);
        } catch (URISyntaxException e) {
            throw new ImplerException("Cannot generate classpath during compilation: " + token + " in root " + root);
        }
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        checkImplementable(token, jarFile);

        Path tempDirectory = jarFile.toAbsolutePath().getParent().resolve("implementorTemp");

        try {
            writeImplementation(token, tempDirectory);
            if (compileFile(token, tempDirectory) != 0) {
                throw new ImplerException("Cannot compile file implementation: " + token);
            }

            Manifest manifest = new Manifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
                String file = getFile(token, ".class").toString();
                jarOutputStream.putNextEntry(new JarEntry(file.replace("\\", "/")));
                Files.copy(tempDirectory.resolve(file), jarOutputStream);
            }
        } catch (IOException e) {
            throw new ImplerException("Cannot write files to jar: " + e.getMessage());
        } finally {
            deleteDirectory(tempDirectory);
        }
    }

    private void deleteDirectory(Path tempDirectory) {
        try {
            Files.walkFileTree(tempDirectory, DELETE_VISITOR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        checkImplementable(token, root);
        writeImplementation(token, root);
    }

    private void writeImplementation(Class<?> token, Path root) throws ImplerException {
        Path file = root.resolve(getFile(token, ".java"));
        createDirectories(file);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writePackage(writer, token);
            writeClass(writer, token);
        } catch (IOException e) {
            throw new ImplerException("Cannot write java file to " + file);
        }
    }

    private void createDirectories(Path file) {
        Path parent = file.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException ignored) {
                // empty
            }
        }
    }

    private void writePackage(BufferedWriter writer, Class<?> token) throws IOException {
        String pack = token.getPackage().getName();
        if (!pack.isEmpty()) {
            write(writer, "package " + pack + ";");
            writeLine(writer);
        }
    }

    private void writeClass(BufferedWriter writer, Class<?> token) throws IOException, ImplerException {
        writeLine(writer, getClassHead(token));
        openSection(writer);
        if (!token.isInterface()) {
            writeConstructors(writer, token);
        }
        writeAbstractMethods(writer, token);
        closeSection(writer);
    }

    private String getClassHead(Class<?> token) {
        return "public class " + getClassName(token) + " " +
                (token.isInterface() ? "implements " : "extends ") + token.getCanonicalName();
    }

    private void writeConstructors(BufferedWriter writer, Class<?> token) throws IOException, ImplerException {
        boolean hasEmpty = Arrays.stream(token.getDeclaredConstructors())
                .anyMatch(x -> x.getParameterTypes().length == 0);
        List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(x -> !Modifier.isPrivate(x.getModifiers()))
                .collect(Collectors.toList());

        if (hasEmpty && constructors.isEmpty()) {
            throw new ImplerException("Cannot implement class without public constructors");
        }

        for (Constructor<?> constructor : token.getDeclaredConstructors()) {
            writeConstructor(writer, constructor);
        }
    }

    private void writeConstructor(BufferedWriter writer, Constructor<?> constructor) throws IOException {
        writeLine(writer);
        writeLine(writer, getConstructorHead(constructor));
        openSection(writer);
        writeLine(writer, getConstructorBody(constructor));
        closeSection(writer);
    }

    private String getConstructorHead(Constructor<?> constructor) {
        return getModifier(constructor) + " " + getClassName(constructor.getDeclaringClass()) +
                "(" + getParams(constructor, true) + ")" + getExceptions(constructor);
    }

    private String getConstructorBody(Constructor<?> constructor) {
        return "super(" + getParams(constructor, false) + ");";
    }

    private void writeAbstractMethods(BufferedWriter writer, Class<?> token) throws IOException {
        HashSet<WrappedMethod> abstractMethods = new HashSet<>(getAbstractMethods(token.getMethods()));
        HashSet<WrappedMethod> definedMethods = Arrays.stream(token.getMethods())
                .filter(Predicate.not(IS_ABSTRACT))
                .map(WrappedMethod::new)
                .collect(Collectors.toCollection(HashSet::new));

        while (token != null) {
            abstractMethods.addAll(getAbstractMethods(token.getDeclaredMethods()));
            token = token.getSuperclass();
        }

        abstractMethods.removeAll(definedMethods);
        for (WrappedMethod abstractMethod : abstractMethods) {
            writeAbstractMethod(writer, abstractMethod.method);
        }
    }

    private void writeAbstractMethod(BufferedWriter writer, Method method) throws IOException {
        writeLine(writer);
        writeLine(writer, getMethodHead(method));
        openSection(writer);
        writeLine(writer, getDefaultMethodBody(method));
        closeSection(writer);
    }

    private List<WrappedMethod> getAbstractMethods(Method[] methods) {
        return Arrays.stream(methods)
                .filter(IS_ABSTRACT)
                .map(WrappedMethod::new)
                .collect(Collectors.toList());
    }

    private String getMethodHead(Method method) {
        return getModifier(method) + " " + getReturnTypeAndNameMethod(method) +
                "(" + getParams(method, true) + ")" + getExceptions(method);
    }

    private String getDefaultMethodBody(Method method) {
        String body = "return";
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(void.class)) {
            return "";
        } else if (returnType.equals(boolean.class)) {
            body = body + " false";
        } else if (returnType.isPrimitive()) {
            body = body + " 0";
        } else {
            body = body + " null";
        }
        return body + ";";
    }

    private String getReturnTypeAndNameMethod(Method method) {
        return method.getReturnType().getCanonicalName() + " " + method.getName();
    }

    private String getExceptions(Executable executable) {
        Class<?>[] exceptionTypes = executable.getExceptionTypes();

        if (exceptionTypes.length == 0) {
            return "";
        } else {
            return " throws " + Arrays.stream(exceptionTypes)
                    .map(Class::getCanonicalName)
                    .collect(Collectors.joining(", "));
        }
    }

    private String getParams(Executable executable, boolean typeRequired) {
        Class<?>[] parameterTypes = executable.getParameterTypes();
        final int[] argN = {0};
        return Arrays.stream(parameterTypes)
                .map(p -> (typeRequired ? p.getCanonicalName() + " " : "") + "arg" + argN[0]++)
                .collect(Collectors.joining(", "));
    }

    private String getModifier(Executable executable) {
        int modifiers = executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT;
        return Modifier.toString(modifiers);
    }

    private void openSection(BufferedWriter writer) throws IOException {
        write(writer, " {");
        section++;
    }

    private void closeSection(BufferedWriter writer) throws IOException {
        section--;
        writeLine(writer, "}");
    }

    private void write(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
    }

    private void writeLine(BufferedWriter writer) throws IOException {
        writer.newLine();
    }

    private void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.newLine();
        write(writer, TAB.repeat(section) + line);
    }

    record WrappedMethod(Method method) {

        @Override
        public int hashCode() {
            return Objects.hash(method.getName(), method.getReturnType(), Arrays.hashCode(method.getParameterTypes()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (obj instanceof WrappedMethod wm) {
                return method.getName().equals(wm.method.getName()) &&
                        method.getReturnType().equals(wm.method.getReturnType())
                        && Arrays.equals(method.getParameterTypes(), wm.method.getParameterTypes());
            } else {
                return false;
            }
        }
    }
}
