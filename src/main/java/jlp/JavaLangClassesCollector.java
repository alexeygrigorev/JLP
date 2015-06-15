package jlp;

import java.io.File;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JavaLangClassesCollector {

    public static void main(String[] args) throws Exception {
        JarFile jarFile = new JarFile("C:/Program Files/Java/jre1.8.0_40/lib/rt.jar");
        int extensionLen = ".class".length();

        PrintWriter pw = new PrintWriter(new File("java.lang.txt"));

        Enumeration<JarEntry> allEntries = jarFile.entries();
        while (allEntries.hasMoreElements()) {
            JarEntry entry = allEntries.nextElement();
            String name = entry.getName();
            String className = name.replace('/', '.').replace('\\', '.');
            className = className.substring(0, className.length() - extensionLen);

            int dots = className.split("\\.").length - 1;

            if (!className.startsWith("java.lang") || dots != 2 || className.contains("$")) {
                continue;
            }

            pw.println(className);
        }

        System.out.println("Done");
        pw.close();
        jarFile.close();
    }

}
