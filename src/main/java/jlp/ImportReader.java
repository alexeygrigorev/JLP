package jlp;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.Maps;

public class ImportReader {

    private Map<String, String> javaLangImports = readJavaLangImports();

    private static Map<String, String> readJavaLangImports() {
        try {
            Map<String, String> imports = Maps.newLinkedHashMap();
            List<String> javaLangClasses = FileUtils.readLines(new File("java.lang.txt"));
            for (String cls : javaLangClasses) {
                String shortName = cls.substring("java.lang.".length(), cls.length());
                imports.put(shortName, cls);
            }
            return imports;
        } catch (Throwable e) {
            return Maps.newLinkedHashMap();
        }
    }

    public ImportReader() {
    }

    /**
     * Will NOT handle wildcards
     */
    public Map<String, String> read(CompilationUnit cu) {
        Map<String, String> result = Maps.newLinkedHashMap();
        result.putAll(javaLangImports);

        ImportCollectorVisitor collector = new ImportCollectorVisitor();
        collector.visit(cu, null);
        result.putAll(collector.getImportMap());

        return result;
    }

    private static class ImportCollectorVisitor extends VoidVisitorAdapter<Void> {
        private Map<String, String> importMap = Maps.newLinkedHashMap();

        @Override
        public void visit(QualifiedNameExpr n, Void arg) {
            String shortName = n.getName();
            importMap.put(shortName, n.toStringWithoutComments());
        }

        public Map<String, String> getImportMap() {
            return importMap;
        }
    }
}
