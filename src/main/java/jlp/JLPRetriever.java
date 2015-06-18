package jlp;

import java.io.File;
import java.util.Collections;
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
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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

        InnerTypeVisitor itp = new InnerTypeVisitor();
        itp.visit(cu, "");

        DeclarationTypeNameVisitor dtv = new DeclarationTypeNameVisitor(imports, packageName,
                itp.innerTypesFullNames);
        dtv.visit(cu, "");

        String arg = extractparamFromFilePath(file.getAbsolutePath());
        return new ClassIdentifiers(packageName, arg, dtv.result);
    }

    private String extractparamFromFilePath(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        int lastBackSlash = filePath.lastIndexOf('\\');
        if (lastBackSlash > lastSlash) {
            lastSlash = lastBackSlash;
        }

        return filePath.substring(lastSlash + 1, filePath.length() - ".java".length());
    }

    private static class DeclarationTypeNameVisitor extends VoidVisitorAdapter<String> {

        private static final Set<String> PRIMITIVES = ImmutableSet.of("byte", "short", "int", "long",
                "float", "double", "boolean", "char");

        private Map<String, String> imports;
        private String packageName;
        private Map<String, String> innerTypesFullNames;

        private Set<String> typeParameters = Sets.newLinkedHashSet();
        private List<Identifier> result = Lists.newArrayList();

        public DeclarationTypeNameVisitor(Map<String, String> imports, String packageName,
                Map<String, String> innerTypesFullNames) {
            this.imports = imports;
            this.packageName = packageName;
            this.innerTypesFullNames = innerTypesFullNames;
        }

        @Override
        public void visit(VariableDeclarationExpr varDeclaration, String arg) {
            Type type = varDeclaration.getType();
            List<VariableDeclarator> variables = varDeclaration.getVars();
            processDeclaration(type, variables, Location.LOCAL);
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration classDec, String arg) {
            List<TypeParameter> params = denull(classDec.getTypeParameters());
            for (TypeParameter tp : params) {
                typeParameters.add(tp.getName());
            }
            super.visit(classDec, arg);
            for (TypeParameter tp : params) {
                typeParameters.remove(tp.getName());
            }
        }

        @Override
        public void visit(FieldDeclaration field, String arg) {
            Type type = field.getType();
            List<VariableDeclarator> variables = field.getVariables();
            processDeclaration(type, variables, Location.FIELD);
        }

        @Override
        public void visit(MethodDeclaration method, String arg) {
            List<TypeParameter> params = denull(method.getTypeParameters());
            for (TypeParameter tp : params) {
                typeParameters.add(tp.getName());
            }
            super.visit(method, arg);
            for (TypeParameter tp : params) {
                typeParameters.remove(tp.getName());
            }
        }

        public void visit(Parameter parameter, String arg) {
            Type type = parameter.getType();
            VariableDeclaratorId id = parameter.getId();
            String shortTypeName = type.toStringWithoutComments();
            if (parameter.isVarArgs()) {
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

            if (typeParameters.contains(shortTypeName)) {
                ComplexType type = new ComplexType("GENERIC_PARAM");
                type.setParameters(shortTypeName); 
                return type;
            }

            if (isFullyQualified(shortTypeName)) {
                return handleFullTypeName(shortTypeName);
            }

            String typeName = shortTypeName;
            if (innerTypesFullNames.containsKey(shortTypeName)) {
                typeName = innerTypesFullNames.get(shortTypeName);
            }

            if (imports.containsKey(typeName)) {
                String fullName = imports.get(typeName);
                return new ComplexType(fullName);
            }

            return handleTypeNameSpecialCases(typeName);
        }

        private ComplexType handleFullTypeName(String fullName) {
            // when it has generic arg
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
            if (typeName.contains(".")) {
                return !startsWithUpperChar(typeName);
            } else {
                return false;
            }
        }

        private boolean startsWithUpperChar(String typeName) {
            // let's hope the code follows the convention
            char firstChar = typeName.charAt(0);
            return Character.toUpperCase(firstChar) == firstChar;
        }
    }

    private static class InnerTypeVisitor extends VoidVisitorAdapter<String> {
        private final Map<String, String> innerTypesFullNames = Maps.newLinkedHashMap();

        @Override
        public void visit(ClassOrInterfaceDeclaration n, String arg) {
            if (arg.isEmpty()) {
                String name = n.getName();
                super.visit(n, name);
            } else {
                String name = arg + "." + n.getName();
                innerTypesFullNames.put(n.getName(), name);
                super.visit(n, name);
            }
        }

    }

    private static <E> List<E> denull(List<E> list) {
        if (list == null) {
            return Collections.emptyList();
        } else {
            return list;
        }
    }

}
