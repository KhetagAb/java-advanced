package info.kgeorgiy.ja.dzestelov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Implementor implements Impler {

    private static final String TAB = "    ";
    private static final String FILE_NAME_SUFFIX = "Impl";

    private static final Predicate<Method> IS_ABSTRACT = x -> Modifier.isAbstract(x.getModifiers());

    private int section = 0;

    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + FILE_NAME_SUFFIX;
    }

    private static Path getJavaFilePath(Class<?> token, Path root) {
        return root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(getClassName(token) + ".java");
    }

    private static boolean isImplementable(Class<?> token) {
        return !Modifier.isFinal(token.getModifiers()) && !token.isPrimitive() && !token.equals(Enum.class) && !Modifier.isPrivate(token.getModifiers());
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Arguments must be not null");
        }

        if (!isImplementable(token)) {
            throw new ImplerException("Cannot implement " + token);
        }

        root = getJavaFilePath(token, root);
        Path parent = root.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException ignored) {
                // empty
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(root)) {
            writePackage(writer, token);
            writeClassName(writer, token);

            openSection(writer);
            if (!token.isInterface()) {
                writeConstructors(writer, token);
            }
            writeAbstractMethods(writer, token);
            closeSection(writer);

        } catch (IOException e) {
            throw new ImplerException("Cannot write java file to " + root);
        }
    }

    private void writePackage(BufferedWriter writer, Class<?> token) throws IOException {
        String pack = token.getPackage().getName();
        if (!pack.isEmpty()) {
            write(writer, "package " + pack + ";");
            writeLine(writer);
        }
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
            writeLine(writer, getConstructor(constructor));
        }
    }

    private String getConstructor(Constructor<?> constructor) {
        Section section = new Section(this.section);
        appendConstructorName(section, constructor);
        appendConstructorBody(section, constructor);
        return section.toString();
    }

    private void appendConstructorBody(Section section, Constructor<?> constructor) {
        section.openSection();
        section.writeLine("super(" + getParams(constructor, false) + ");");
        section.closeSection();
    }

    private void appendConstructorName(Section section, Constructor<?> constructor) {
        section.write(getModifier(constructor) + " " + getClassName(constructor.getDeclaringClass()) + " (" + getParams(constructor, true) + ")" + getExceptions(constructor));
    }

    private void writeAbstractMethods(BufferedWriter writer, Class<?> token) throws IOException { // toDo
        HashSet<WrappedMethod> abstractMethods = new HashSet<>(getAbstractMethods(token.getMethods()));
        HashSet<WrappedMethod> definedMethods = Arrays.stream(token.getMethods()).map(WrappedMethod::new).collect(Collectors.toCollection(HashSet::new));

        while (token != null) {
            abstractMethods.addAll(getAbstractMethods(token.getDeclaredMethods()));
            token = token.getSuperclass();
        }

        for (WrappedMethod abstractMethod : abstractMethods) {
            writeLine(writer, getAbstractMethod(abstractMethod.method));
        }
    }

    private List<WrappedMethod> getAbstractMethods(Method[] methods) {
        return Arrays.stream(methods)
                .filter(IS_ABSTRACT)
                .map(WrappedMethod::new)
                .collect(Collectors.toList());
    }

    private String getAbstractMethod(Method method) {
        Section section = new Section(this.section);
        appendMethodName(section, method);
        appendMethodBody(section, method);
        return section.toString();
    }

    private void appendMethodName(Section section, Method method) {
        section.write(getModifier(method) + " " + getReturnTypeAndNameMethod(method) + " (" + getParams(method, true) + ")" + getExceptions(method));
    }

    private void appendMethodBody(Section section, Method method) {
        section.openSection();
        Class<?> returnType = method.getReturnType();
        section.writeLine("return");
        if (returnType.equals(Boolean.TYPE)) {
            section.write(" false");
        } else if (returnType.equals(Void.TYPE)) {
            section.write("");
        } else if (returnType.isPrimitive()) {
            section.write(" 0");
        } else {
            section.write(" null");
        }
        section.write(";");
        section.closeSection();
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
                .map(p -> (typeRequired ? p.getCanonicalName() + " " : "") + "args" + argN[0]++)
                .collect(Collectors.joining(", "));
    }

    private String getModifier(Executable executable) {
        int modifiers = executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT;
        return Modifier.toString(modifiers);
    }

    private void writeClassName(BufferedWriter writer, Class<?> token) throws IOException {
        writeLine(writer, "public class " + getClassName(token) + " " + (token.isInterface() ? "implements " : "extends ") + token.getCanonicalName());
    }

    private void openSection(BufferedWriter writer) throws IOException {
        writeLine(writer, "{");
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

    static class WrappedMethod {
        private final Method method;

        WrappedMethod(Method method) {
            this.method = method;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(method);
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

    static class Section {
        private final StringBuilder sb;
        private int section;

        Section(int section) {
            this.section = section;
            this.sb = new StringBuilder();
        }

        void openSection() {
            writeLine("{");
            section++;
        }

        private void closeSection() {
            section--;
            writeLine("}");
        }

        private Section write(String line) {
            sb.append(line);
            return this;
        }

        private Section writeLine(String line) {
            sb.append(System.lineSeparator());
            write(TAB.repeat(section) + line);
            return this;
        }

        public String toString() {
            return sb.toString();
        }
    }
}
