package jlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import jlp.pojos.ClassIdentifiers;
import jlp.pojos.Identifier;
import jlp.pojos.Identifier.Location;

import org.junit.Test;

import com.google.common.collect.Maps;

public class JLPRetrieverTest {

    @Test
    public void test() throws Exception {
        String path = "./src/test/java/jlp/TestClass.java";
        ClassIdentifiers ids = JLPRetriever.INSTANCE.read(path);

        assertEquals("TestClass", ids.getClassName());
        assertEquals("jlp", ids.getPackageName());

        Map<String, Identifier> index = buildIndex(ids);

        Identifier mapField = index.get("mapField");
        assertEquals(Location.FIELD, mapField.getLocation());
        assertTrue(mapField.getType().isParametrized());
        assertEquals("java.util.Map", mapField.getType().getFullName());
        assertEquals("<String, String>", mapField.getType().getParameters());

        Identifier listField = index.get("listField");
        assertEquals(Location.FIELD, listField.getLocation());
        assertTrue(listField.getType().isParametrized());
        assertEquals("java.util.List", listField.getType().getFullName());
        assertEquals("<Set<String>>", listField.getType().getParameters());

        Identifier stringField = index.get("stringField");
        assertEquals(Location.FIELD, stringField.getLocation());
        // it's in java.lang although there's no explicit import
        assertEquals("java.lang.String", stringField.getType().getFullName());

        Identifier primitiveConstructorParam = index.get("primitiveConstructorParam");
        assertEquals(Location.METHOD_PARAM, primitiveConstructorParam.getLocation());
        assertEquals("int", primitiveConstructorParam.getType().getFullName());

        Identifier constructorParam = index.get("constructorParam");
        assertEquals(Location.METHOD_PARAM, constructorParam.getLocation());
        assertEquals("java.lang.String", constructorParam.getType().getFullName());

        Identifier innerClassMethodParam = index.get("innerClassMethodParam");
        assertEquals(Location.METHOD_PARAM, innerClassMethodParam.getLocation());
        // it's in the package so "jlp" gets appended
        assertEquals("jlp.TestClass.SomeInnerClass", innerClassMethodParam.getType().getFullName());

        Identifier otherClassMethodParam = index.get("otherClassMethodParam");
        assertEquals(Location.METHOD_PARAM, otherClassMethodParam.getLocation());
        // it's in the package so "jlp" gets appended
        assertEquals("jlp.SomeClass", otherClassMethodParam.getType().getFullName());

        Identifier varargMethodParam = index.get("varargMethodParam");
        assertEquals(Location.METHOD_PARAM, varargMethodParam.getLocation());
        assertTrue(varargMethodParam.getType().isArray());
        assertEquals("java.lang.String", varargMethodParam.getType().getFullName());
        assertEquals("...", varargMethodParam.getType().getArrayType());

        Identifier args = index.get("args");
        assertEquals(Location.METHOD_PARAM, args.getLocation());
        assertTrue(args.getType().isArray());
        assertEquals("java.lang.String", args.getType().getFullName());
        assertEquals("[]", args.getType().getArrayType());

        Identifier listLocal = index.get("listLocal");
        assertEquals(Location.LOCAL, listLocal.getLocation());
        assertTrue(listLocal.getType().isParametrized());
        assertEquals("java.util.List", listLocal.getType().getFullName());
        assertEquals("<String>", listLocal.getType().getParameters());

        // can capture loop variables as well
        Identifier loopVariable = index.get("loopVariable");
        assertEquals(Location.LOCAL, loopVariable.getLocation());
        assertEquals("java.lang.String", loopVariable.getType().getFullName());

        // can use fully qualified name for java.lang classes
        Identifier stringWithFullyQualifiedName = index.get("stringWithFullyQualifiedName");
        assertEquals(Location.LOCAL, stringWithFullyQualifiedName.getLocation());
        assertEquals("java.lang.String", stringWithFullyQualifiedName.getType().getFullName());

        // or for any other package
        Identifier variableWithFullyQualifiedName = index.get("variableWithFullyQualifiedName");
        assertEquals(Location.LOCAL, variableWithFullyQualifiedName.getLocation());
        assertEquals("org.w3c.dom.Node", variableWithFullyQualifiedName.getType().getFullName());
    }

    @Test
    public void varArg() throws Exception {
        String path = "./src/test/java/jlp/TestVarArg.java";
        ClassIdentifiers ids = JLPRetriever.INSTANCE.read(path);

        Identifier vararg = ids.getIds().get(0);

        assertEquals(Location.METHOD_PARAM, vararg.getLocation());
        assertTrue(vararg.getType().isArray());
        assertEquals("java.lang.String", vararg.getType().getFullName());
        assertEquals("...", vararg.getType().getArrayType());
    }

    @Test
    public void innerClassesDotted() throws Exception {
        String path = "./src/test/java/jlp/TestClass.java";
        ClassIdentifiers ids = JLPRetriever.INSTANCE.read(path);
        Map<String, Identifier> index = buildIndex(ids);

        Identifier innerClass = index.get("innerClassMethodParam");
        assertEquals(Location.METHOD_PARAM, innerClass.getLocation());
        assertEquals("jlp.TestClass.SomeInnerClass", innerClass.getType().getFullName());

        Identifier innerClassMethodParamShortTypeName = index.get("innerClassMethodParamShortTypeName");
        assertEquals(Location.METHOD_PARAM, innerClassMethodParamShortTypeName.getLocation());
        assertEquals("jlp.TestClass.SomeInnerClass", innerClassMethodParamShortTypeName.getType()
                .getFullName());
    }

    @Test
    public void typeParameters() throws Exception {
        String path = "./src/test/java/jlp/GenericTest.java";
        ClassIdentifiers ids = JLPRetriever.INSTANCE.read(path);

        Identifier p1 = ids.getIds().get(0);
        assertEquals("GENERIC_PARAM", p1.getType().getFullName());

        Identifier p2 = ids.getIds().get(1);
        assertEquals("GENERIC_PARAM", p2.getType().getFullName());
    }

    private Map<String, Identifier> buildIndex(ClassIdentifiers ids) {
        Map<String, Identifier> index = Maps.newHashMap();
        for (Identifier id : ids.getIds()) {
            index.put(id.getName(), id);
        }
        return index;
    }

}
