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

/**
 * Implementation for {@link JarImpler} interface.
 */
public class Implementor implements JarImpler {

    /**
     * Tab fields.
     */
    private static final String TAB = "    ";

    /**
     * Suffix for implementation file.
     */
    private static final String FILE_NAME_SUFFIX = "Impl";

    /**
     * Visitor for recursive files deleting.
     */
    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {

        /**
         * Delete file.
         *
         * @param file current visited file
         * @param attrs file attributes
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException If an I/O error occurs
         */
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Delete empty directory.
         *
         * @param dir current visited directory
         * @param exc {@code null} if the iteration of the directory completes without
         *              an error; otherwise the I/O exception that caused the iteration
         *              of the directory to complete prematurely
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException If an I/O error occurs
         */
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Predicate to detect abstract {@link Method}.
     */
    private static final Predicate<Method> IS_ABSTRACT = x -> Modifier.isAbstract(x.getModifiers());

    /**
     * Nesting level of actual section.
     */
    private int section = 0;

    /**
     * Get class {@link Class#getSimpleName()} with {@value #FILE_NAME_SUFFIX}.
     * @param token token of class
     * @return class name with suffix
     */
    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + FILE_NAME_SUFFIX;
    }

    /**
     * Get resolved path. Resolve <tt>root</tt> against {@link #getFile(Class, String, Character)} with {@link File#separatorChar} character.
     * @param root root to resolve path
     * @param token class to get file name
     * @param end suffix used in {@link #getFile(Class, String, Character)}
     * @return resolved path
     */
    private static Path getResolveFile(Path root, Class<?> token, String end) {
        return root.resolve(getFile(token, end, File.separatorChar));
    }

    /**
     * Get class implementation package name. Separate {@link #getClassName(Class)} by <tt>separator</tt> with <tt>end</tt> suffix.
     * @param token class to get impl package name
     * @param end added suffix
     * @param separator separator used in impl package name
     * @return class package impl name
     */
    private static String getFile(Class<?> token, String end, Character separator) {
        return token.getPackageName().replace('.', separator) + separator + getClassName(token) + end;
    }

    /**
     * Function to run {@link Implementor}: Without <tt>-jar</tt> flag requires two arguments:
     * class <tt>className</tt> to implement and <tt>root</tt> to save class implementation.
     * With <tt>-jar</tt> flag requires <tt>className</tt> and <tt>jarFile</tt> file - name of the jar file to create.
     *
     * @param args arguments to run application
     */
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

    /**
     * Check class <tt>token</tt> and path <tt>root</tt> to be implementable.
     * Check if arguments is not null, <tt>token</tt> is not final, primitive, {@link Enum} or private.
     * @param token class
     * @param root path to save class implementation
     * @throws ImplerException If it cannot be implemented
     */
    private static void checkImplementable(Class<?> token, Path root) throws ImplerException {
        if (root == null) {
            throw new ImplerException("Root must be not null");
        }
        if (token == null || Modifier.isFinal(token.getModifiers()) || token.isPrimitive() || token.equals(Enum.class) || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Cannot implement " + token);
        }
    }

    /**
     * Compile file class <tt>token</tt> implementation to <tt>dir</tt> directory. Use class <tt>token</tt> classpath.
     * @param token class to compile implementation
     * @param dir directory to store class files
     * @return 0 for success; nonzero otherwise
     * @throws ImplerException If could not find java compiler or generate classpath
     */
    private static int compileFile(Class<?> token, Path dir) throws ImplerException {
        try {
            final String classpath = dir + File.pathSeparator + Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
            final String[] args = new String[]{getResolveFile(dir, token, ".java").toString(), "-cp", classpath};
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new ImplerException("Could not find java compiler, include tools.jar to classpath");
            }
            return compiler.run(null, null, null, args);
        } catch (URISyntaxException e) {
            throw new ImplerException("Cannot generate classpath during compilation: " + token + " in dir " + dir);
        }
    }


    /**
     * @throws ImplerException If <var>.jar</var> file cannot be generated
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (jarFile == null) {
            throw new ImplerException("jarFile must not be null");
        }

        Path tempDirectory = jarFile.toAbsolutePath().getParent().resolve("implementorTemp");
        try {
            implement(token, tempDirectory);
            if (compileFile(token, tempDirectory) != 0) {
                throw new ImplerException("Cannot compile class file implementation: " + token);
            }

            Manifest manifest = new Manifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
                jarOutputStream.putNextEntry(new JarEntry(getFile(token, ".class", '/')));
                Files.copy(getResolveFile(tempDirectory, token, ".class"), jarOutputStream);
            }
        } catch (IOException e) {
            throw new ImplerException("Cannot write files to jar: " + e.getMessage());
        } finally {
            deleteDirectory(tempDirectory);
        }
    }

    /**
     * Delete directory recursively. Uses {@link Files#walkFileTree(Path, FileVisitor)} with {@link #DELETE_VISITOR}.
     * @param tempDirectory directory to delete
     */
    private void deleteDirectory(Path tempDirectory) {
        try {
            Files.walkFileTree(tempDirectory, DELETE_VISITOR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws ImplerException If implementation file cannot be generated
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        checkImplementable(token, root);

        Path file = getResolveFile(root, token, ".java");
        createDirectories(file);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            String pack = token.getPackage().getName();
            if (!pack.isEmpty()) {
                write(writer, "package " + pack + ";");
                writeLine(writer);
            }

            writeLine(writer, getClassHead(token));
            openSection(writer);
            if (!token.isInterface()) {
                writeConstructors(writer, token);
            }
            writeAbstractMethods(writer, token);
            closeSection(writer);
        } catch (IOException e) {
            throw new ImplerException("Cannot write java file to " + file);
        }
    }

    /**
     * Create directories to file
     * @param file given file
     */
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

    /**
     * Get class headline. Headline of public class <tt>token</tt> implementation.
     * @param token class to implement
     * @return string of public class headline
     */
    private String getClassHead(Class<?> token) {
        return "public class " + getClassName(token) + " " +
                (token.isInterface() ? "implements " : "extends ") + token.getCanonicalName();
    }

    /**
     * Write fully generated code of class <tt>token</tt> constructors to <tt>writer</tt>. Uses {@link #writeConstructor(BufferedWriter, Constructor)}.
     * @param writer writer, to write constructors code
     * @param token class
     * @throws IOException If an I/O error occurs
     * @throws ImplerException If class cannot be implemented
     */
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

    /**
     * Write generated code of class <tt>token</tt> constructor to <tt>writer</tt>.
     * @param writer writer, to write constructor code
     * @param constructor class's constructor
     * @throws IOException If an I/O error occurs
     */
    private void writeConstructor(BufferedWriter writer, Constructor<?> constructor) throws IOException {
        writeLine(writer);
        writeLine(writer, getConstructorHead(constructor));
        openSection(writer);
        writeLine(writer, getConstructorBody(constructor));
        closeSection(writer);
    }

    /**
     * Get constructor headline. Generate constructor's code with access modifier, class name, params and exceptions
     * @param constructor constructor to generate headline
     * @return string of constructor headline
     */
    private String getConstructorHead(Constructor<?> constructor) {
        return getModifier(constructor) + " " + getClassName(constructor.getDeclaringClass()) +
                "(" + getParams(constructor, true) + ")" + getExceptions(constructor);
    }

    /**
     * Get default constructor body.
     * @param constructor constructor to generate body
     * @return string of constructor body
     */
    private String getConstructorBody(Constructor<?> constructor) {
        return "super(" + getParams(constructor, false) + ");";
    }

    /**
     * Write fully generated code of class <tt>token</tt> abstract methods to <tt>writer</tt>. Uses {@link #writeAbstractMethod(BufferedWriter, Method)}.
     * @param writer writer, to write abstract methods code
     * @param token class
     * @throws IOException If an I/O error occurs
     */
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

    /**
     * Write generated code of <tt>method</tt> to <tt>writer</tt>.
     * @param writer writer, to write constructor code
     * @param method class's method
     * @throws IOException If an I/O error occurs
     */
    private void writeAbstractMethod(BufferedWriter writer, Method method) throws IOException {
        writeLine(writer);
        writeLine(writer, getMethodHead(method));
        openSection(writer);
        writeLine(writer, getDefaultMethodBody(method));
        closeSection(writer);
    }

    /**
     * Convert {@link Method[]} to {@link List<WrappedMethod>}, filtering by {@link #IS_ABSTRACT} predicate.
     * @param methods array of methods
     * @return list of wrapped methods
     */
    private List<WrappedMethod> getAbstractMethods(Method[] methods) {
        return Arrays.stream(methods)
                .filter(IS_ABSTRACT)
                .map(WrappedMethod::new)
                .collect(Collectors.toList());
    }

    /**
     * Get method headline. Generate method's code with access modifier, return type, method name, params and exceptions
     * @param method method to generate headline
     * @return string of method headline
     */
    private String getMethodHead(Method method) {
        return getModifier(method) + " " + getReturnTypeAndNameMethod(method) +
                "(" + getParams(method, true) + ")" + getExceptions(method);
    }

    /**
     * Get default method body.
     * @param method method to generate body
     * @return string of method body
     */
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

    /**
     * Get return type and method name.
     * @param method method to get return type and name
     * @return return type and method name
     */
    private String getReturnTypeAndNameMethod(Method method) {
        return method.getReturnType().getCanonicalName() + " " + method.getName();
    }

    /**
     * Get {@link Executable} thrown exceptions.
     * @param executable executable to get exceptions
     * @return string of thrown exceptions
     */
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

    /**
     * Get {@link Executable} params.
     * @param executable executable to get params
     * @param typeRequired If true, params types will provide
     * @return string of params
     */
    private String getParams(Executable executable, boolean typeRequired) {
        Class<?>[] parameterTypes = executable.getParameterTypes();
        final int[] argN = {0};
        return Arrays.stream(parameterTypes)
                .map(p -> (typeRequired ? p.getCanonicalName() + " " : "") + "arg" + argN[0]++)
                .collect(Collectors.joining(", "));
    }

    /**
     * Get {@link Executable} modifier. Ignore abstract and transient modifiers.
     * @param executable executable to get modifier
     * @return string of modifier
     */
    private String getModifier(Executable executable) {
        int modifiers = executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT;
        return Modifier.toString(modifiers);
    }

    /**
     * Write open bracket to <tt>writer</tt> and increase {@link #section} counter.
     * @param writer writer to write bracket
     * @throws IOException If an I/O error occurs
     */
    private void openSection(BufferedWriter writer) throws IOException {
        write(writer, " {");
        section++;
    }

    /**
     * Decrease {@link #section} counter and write close bracket to <tt>writer</tt>.
     * @param writer writer to write bracket
     * @throws IOException If an I/O error occurs
     */
    private void closeSection(BufferedWriter writer) throws IOException {
        section--;
        writeLine(writer, "}");
    }

    /**
     * Write <tt>line</tt> to <tt>writer</tt>.
     * @param writer writer to write line
     * @param line line to be written
     * @throws IOException If an I/O error occurs
     */
    private void write(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
    }

    /**
     * Write new line to <tt>writer</tt>.
     * @param writer writer to write new line
     * @throws IOException If an I/O error occurs
     */
    private void writeLine(BufferedWriter writer) throws IOException {
        writer.newLine();
    }

    /**
     * Write <tt>line</tt> to <tt>writer</tt> on new line corrected on {@link #section} counter.
     * @param writer writer to write new line
     * @throws IOException If an I/O error occurs
     */
    private void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.newLine();
        write(writer, TAB.repeat(section) + line);
    }

    /**
     * Wrapper record to sort methods
     */
    record WrappedMethod(Method method) {

        /**
         * Hashcode agreed with equals method.
         * @return hash of name, return type and parameter types
         */
        @Override
        public int hashCode() {
            return Objects.hash(method.getName(), method.getReturnType(), Arrays.hashCode(method.getParameterTypes()));
        }

        /**
         * Compares object with this wrapper foe equality.
         * Two WrapperMethod are equal if their inner methods have equal name, return type and parameters.
         * @param obj obj to compare with
         * @return true, if obj is equal to wrapper; false otherwise
         */
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
