package instructor;

import AbstractMath.Rings.Structures.Polynomial.Polynomial;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PolynomialTypeSafetyTest {
    @TempDir
    Path output;

    @Test
    void theBoundAndImplementedInterfacePreserveConcreteTypes() throws Exception {
        assertCompiles(true, """
                RingElement<Polynomial<ZModElement<Mod3>>> ring = p;
                Polynomial<ZModElement<Mod3>> sum = ring.add(p);
                Polynomial<ZModElement<Mod3>> product = ring.multiply(p);
                Polynomial<ZModElement<Mod3>> zero = ring.zero();
                Polynomial<ZModElement<Mod3>> one = ring.one();
                Polynomial<ZModElement<Mod3>> negative = ring.negate();
                ZModElement<Mod3> coefficient = p.coefficient(3);
                Polynomial<ZModElement<Mod3>> explicitDegree = new Polynomial<>(1, List.of(a, a));
                ZModElement<Mod3> retrieved = explicitDegree.getValue(1);
                var empty = Polynomial.<ZModElement<Mod3>>builder(1);
                empty.setValue(0, a);
                empty.setValue(1, a);
                Polynomial<ZModElement<Mod3>> built = empty.build();
                var emptyMatrix = Matrix.<ZModElement<Mod3>>builder(1, 1);
                emptyMatrix.setValue(0, 0, a);
                Matrix<ZModElement<Mod3>> builtMatrix = emptyMatrix.build();
                Matrix<Polynomial<ZModElement<Mod3>>> matrix = new Matrix<>(1, 1, List.of(List.of(p)));
                Polynomial<Matrix<ZModElement<Mod3>>> otherStructure = new Polynomial<>(List.of(m));
                Polynomial<Polynomial<ZModElement<Mod3>>> nested = new Polynomial<>(List.of(p));
                """);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "p.add(q);",
            "p.multiply(q);",
            "p.add(a);",
            "p.add(m);",
            "Polynomial<String> invalid = new Polynomial<>(List.of(\"x\"));",
            "Polynomial<ZModElement<Mod3>> mixed = new Polynomial<>(List.of(a, b));",
            "ZModElement<Mod5> wrong = p.coefficient(0);",
            "Polynomial<ZModElement<Mod3>> mixed = new Polynomial<>(1, List.of(a, b));",
            "ZModElement<Mod5> wrong = p.getValue(0);",
            "builder.setValue(1, b);",
            "builder.setValue(1, m);",
            "p.setValue(0, a);",
            "builder.getValue(0);",
            "builder.coefficient(0);",
            "builder.degree();",
            "builder.add(p);"
    })
    void incompatibleTypesAreRejectedByTheCompiler(String statement) throws Exception {
        assertCompiles(false, statement);
    }

    private void assertCompiles(boolean expected, String statements) throws Exception {
        String source = """
                import AbstractMath.Rings.RingElement;
                import AbstractMath.Rings.ZMod.*;
                import AbstractMath.Rings.Structures.Matrix.Matrix;
import AbstractMath.Rings.Structures.Polynomial.Polynomial;
                import java.util.List;

                class Example {
                    void use() {
                        ZModElement<Mod3> a = new ZModElement<>(1, new Mod3());
                        ZModElement<Mod5> b = new ZModElement<>(1, new Mod5());
                        Polynomial<ZModElement<Mod3>> p = new Polynomial<>(List.of(a));
                        Polynomial<ZModElement<Mod5>> q = new Polynomial<>(List.of(b));
                        Matrix<ZModElement<Mod3>> m = new Matrix<>(1, 1, List.of(List.of(a)));
                        var builder = Polynomial.<ZModElement<Mod3>>builder(1);
                """ + statements + "\n    }\n}\n";
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "A JDK is required");
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var file = new SimpleJavaFileObject(URI.create("string:///Example.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) { return source; }
        };
        String classes = Path.of(Polynomial.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        try (var manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            boolean compiled = compiler.getTask(null, manager, diagnostics,
                    List.of("-classpath", classes, "-d", output.toString(), "-proc:none"),
                    null, List.of(file)).call();
            assertEquals(expected, compiled, () -> diagnostics.getDiagnostics().toString());
            if (!expected) {
                assertTrue(diagnostics.getDiagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR));
            }
        }
    }
}
