package jlp;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;

import jlp.pojos.ClassIdentifiers;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jr.ob.JSON;

public class BatchRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchRunner.class);

    public static void main(String[] args) throws Exception {
        batchReadWrite("C:/Users/Alexey Grigorev/Documents/GitHub/mahout/", "c:/tmp/mlp/jlp-mahout.json");
    }

    public static void batchReadWrite(String directory, String pathToOutput) throws Exception {
        LOGGER.info("iterating over files in {}...", directory);

        String[] extensions = { "java" };
        Iterator<File> files = FileUtils.iterateFiles(new File(directory), extensions, true);

        JLPRetriever retriever = JLPRetriever.INSTANCE;
        PrintWriter pw = new PrintWriter(pathToOutput);

        while (files.hasNext()) {
            File next = files.next();
            LOGGER.info("processing {}...", next.getAbsolutePath().substring(directory.length()));

            ClassIdentifiers ids = retriever.read(next);
            pw.println(JSON.std.asString(ids));
        }

        pw.close();
    }

}
