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

    private Map<String, String> mapField;
    private List<Set<String>> listField;
    private String stringField;

    public TestClass(int primitiveConstructorParam, String constructorParam) {
    }

    public void methodName(SomeInnerClass innerClassMethodParam) {
    }

    public void methodName2(SomeClass otherClassMethodParam) {
    }

    public static void main(String[] args) throws Exception {
        List<String> lst = Arrays.asList("a", "b", "c");
        lst.toString();

        java.lang.String fullyQualifiedType = "someString";
        fullyQualifiedType.toString();

        org.w3c.dom.Node fullyQualifiedNameWhenConflict = null;
        if (fullyQualifiedNameWhenConflict == null) {
            System.out.println("Null");
        }

        String path = "C:/Users/Alexey Grigorev/Documents/GitHub/source-code-id-extractor/"
                + "src/main/java/jlp/TestClass.java";
        CompilationUnit cu = JavaParser.parse(new File(path));

        PackageDeclaration packageDeclaration = cu.getPackage();
        System.out.println("package: " + packageDeclaration.getName());

        for (Node loopVariable : cu.getChildrenNodes()) {
            loopVariable.toString();
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
