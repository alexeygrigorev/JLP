package jlp;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jlp.pojos.ClassIdentifiers;
import jlp.pojos.ComplexType;
import jlp.pojos.Identifier;
import jlp.pojos.Identifier.Location;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class JLPRetriever {

    public static final JLPRetriever INSTANCE = new JLPRetriever();

    private final ImportReader importReader = new ImportReader();

    public ClassIdentifiers read(String filePath) throws Exception {
        return read(new File(filePath));
    }

    public ClassIdentifiers read(File file) throws Exception {
        CompilationUnit cu = JavaParser.parse(file);
        Map<String, String> imports = importReader.read(cu);

        PackageDeclaration packageDeclaration = cu.getPackage();
        String packageName = packageDeclaration.getName().toStringWithoutComments();

        DeclarationTypeNameVisitor dtv = new DeclarationTypeNameVisitor(imports, packageName);
        dtv.visit(cu, null);

        String className = extractClassNameFromFilePath(file.getAbsolutePath());
        return new ClassIdentifiers(packageName, className, dtv.result);
    }

    private String extractClassNameFromFilePath(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        int lastBackSlash = filePath.lastIndexOf('\\');
        if (lastBackSlash > lastSlash) {
            lastSlash = lastBackSlash;
        }

        return filePath.substring(lastSlash + 1, filePath.length() - ".java".length());
    }

    private static class DeclarationTypeNameVisitor extends VoidVisitorAdapter<Void> {

        private static final Set<String> PRIMITIVES = ImmutableSet.of("byte", "short", "int", "long",
                "float", "double", "boolean", "char");

        private Map<String, String> imports;
        private String packageName;

        private List<Identifier> result = Lists.newArrayList();

        public DeclarationTypeNameVisitor(Map<String, String> imports, String packageName) {
            this.imports = imports;
            this.packageName = packageName;
        }

        @Override
        public void visit(VariableDeclarationExpr n, Void arg) {
            Type type = n.getType();
            List<VariableDeclarator> variables = n.getVars();
            processDeclaration(type, variables, Location.LOCAL);
        }

        @Override
        public void visit(FieldDeclaration n, Void arg) {
            Type type = n.getType();
            List<VariableDeclarator> variables = n.getVariables();
            processDeclaration(type, variables, Location.FIELD);
        }

        public void visit(Parameter n, Void arg) {
            Type type = n.getType();
            VariableDeclaratorId id = n.getId();
            String shortTypeName = type.toStringWithoutComments();
            if (n.isVarArgs()) {
                shortTypeName = shortTypeName + "...";
            }

            ComplexType typeName = parseTypeName(shortTypeName);
            String idName = id.toStringWithoutComments();
            Identifier foundId = new Identifier(typeName, idName, Location.METHOD_PARAM);

            result.add(foundId);
        }

        private void processDeclaration(Type type, List<VariableDeclarator> variables, Location loc) {
            String shortTypeName = type.toStringWithoutComments();
            ComplexType fullType = parseTypeName(shortTypeName);

            for (VariableDeclarator v : variables) {
                VariableDeclaratorId id = v.getId();
                String idName = id.toStringWithoutComments();
                result.add(new Identifier(fullType, idName, loc));
            }
        }

        private ComplexType parseTypeName(String shortTypeName) {
            if (isPrimitive(shortTypeName)) {
                return new ComplexType(shortTypeName);
            }

            if (isFullyQualified(shortTypeName)) {
                return handleFullTypeName(shortTypeName);
            }

            if (imports.containsKey(shortTypeName)) {
                String fullName = imports.get(shortTypeName);
                return new ComplexType(fullName);
            }

            return handleTypeNameSpecialCases(shortTypeName);
        }

        private ComplexType handleFullTypeName(String fullName) {
            // when it has generic param
            int first = fullName.indexOf('<');
            if (first >= 0) {
                String cleanFullName = fullName.substring(0, first);
                ComplexType type = new ComplexType(cleanFullName);
                type.setParametrized(true);
                String generic = fullName.substring(first, fullName.length());
                type.setParameters(generic);
                return type;
            }

            // when it's an array
            int firstBracket = fullName.indexOf('[');
            if (firstBracket >= 0) {
                String cleanFullName = fullName.substring(0, firstBracket);
                ComplexType type = new ComplexType(cleanFullName);
                type.setArray(true);
                String brackets = fullName.substring(firstBracket, fullName.length());
                type.setArrayType(brackets);
                return type;
            }

            // when it's vararg array
            if (fullName.endsWith("...")) {
                String cleanFullName = fullName.substring(0, fullName.length() - 3);
                ComplexType type = new ComplexType(cleanFullName);
                type.setArray(true);
                type.setArrayType("...");
                return type;
            }

            // else it's fine, just return it
            return new ComplexType(fullName);
        }

        private ComplexType handleTypeNameSpecialCases(String shortTypeName) {
            // when it has generic param
            if (shortTypeName.contains("<")) {
                return parseParametrizedTypeName(shortTypeName);
            }

            // when it's an array
            if (shortTypeName.contains("[")) {
                return parseArrayTypeName(shortTypeName);
            }

            // when it's vararg array
            if (shortTypeName.endsWith("...")) {
                return parseVarargTypeName(shortTypeName);
            }

            // must be from the same package
            String fullName = packageName + "." + shortTypeName;
            return new ComplexType(fullName);
        }

        private ComplexType parseVarargTypeName(String shortName) {
            String clearShortName = shortName.substring(0, shortName.length() - 3);
            ComplexType name = parseTypeName(clearShortName);
            name.setArray(true);
            name.setArrayType("...");
            return name;
        }

        private ComplexType parseArrayTypeName(String shortName) {
            int firstBracket = shortName.indexOf('[');
            String clearShortName = shortName.substring(0, firstBracket);
            ComplexType name = parseTypeName(clearShortName);
            name.setArray(true);

            String brackets = shortName.substring(firstBracket, shortName.length());
            name.setArrayType(brackets);
            return name;
        }

        private ComplexType parseParametrizedTypeName(String shortName) {
            int first = shortName.indexOf('<');

            String cleanShortName = shortName.substring(0, first);
            ComplexType name = parseTypeName(cleanShortName);
            name.setParametrized(true);

            String generic = shortName.substring(first, shortName.length());
            name.setParameters(generic);

            return name;
        }

        private boolean isPrimitive(String typeName) {
            return PRIMITIVES.contains(typeName);
        }

        private boolean isFullyQualified(String typeName) {
            if (typeName.endsWith("...")) {
                return isFullyQualified(typeName.substring(0, typeName.length() - 3));
            }
            return typeName.contains(".");
        }

    }

}
