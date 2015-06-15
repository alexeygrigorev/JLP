package jlp;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;

public class TestClass {

    /* spaces between Map and <String, String> are intentional */ 
    private Map   <String, String> mapField;
    private List /* comment */<Set<String>> listField;
    private String stringField;

    public TestClass(int primitiveConstructorParam, String constructorParam) {
    }

    // TODO: HANDLE
    public void methodName(TestClass.SomeInnerClass innerClassMethodParam) {
    }

    public void methodName2(SomeClass otherClassMethodParam) {
    }

    public void methodName2(String... varargMethodParam) {
    }

    public static void main(String[] args) throws Exception {
        List<String> listLocal = Arrays.asList("a", "b", "c");
        listLocal.toString();

        for (String loopVariable : listLocal) {
            loopVariable.toString();
        }

        java.lang.String stringWithFullyQualifiedName = "someString";
        stringWithFullyQualifiedName.toString();

        org.w3c.dom.Node variableWithFullyQualifiedName = null;
        if (variableWithFullyQualifiedName == null) {
            System.out.println("Null");
        }
    }

    static class SomeInnerClass {

        private final List<Double> innerClassField = null;

        public SomeInnerClass(int innerClassConstructorParam) {
        }

        public List<Integer> someMethod(String innerClassParam) {
            List<Integer> innerClassVariable = Arrays.asList(1, 2, 3);
            return innerClassVariable;
        }

    }

}

class SomeClass {

    private final List<Double> otherClassField = null;

    public SomeClass(int otherClassConstructorParam) {
    }

    public List<Integer> someMethod(String otherClassParam) {
        List<Integer> otherClassVariable = Arrays.asList(1, 2, 3);
        return otherClassVariable;
    }

}
