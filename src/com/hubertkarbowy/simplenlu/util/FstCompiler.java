package com.hubertkarbowy.simplenlu.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.hubertkarbowy.simplenlu.util.RnluSettings.*;

public class FstCompiler {

    public static void compileRules() {
        List<Path> rules;
        // List<Path> rulesDirs = Arrays.stream(new File(fstRootPath).listFiles(File::isDirectory)).map(x -> x.toString() + "/rules/").collect(Collectors.toList());
        //   System.out.println("Compiling rules from " + rulesDirs);
        System.out.println("=== Compiling rules... ===");
        try {
            rules = Files.walk(fstRootPath).filter(x -> x.toString().contains("/root/") && x.toString().endsWith(".txt")).collect(Collectors.toList());

            for (Path rule : rules) {
                Path basename = rule.getFileName();
                String culture = extractCultureFromPath(rule);

                Path compiledFst = rule.getParent().resolve(basename.toString().replaceAll(".txt", ".fst"));
                System.out.println("Compiling " + rule + " into " + compiledFst + " [culture = " + culture + "]");
                String[] cmd = {"/bin/sh", "-c", "fstcompile --isymbols=" + fstRootPath + "/" + culture + "/isyms.txt --osymbols=" + fstRootPath + "/" + culture + "/osyms.txt --keep_isymbols --keep_osymbols " + rule + " " + compiledFst};
                // System.out.println(String.join(" ", cmd));
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
            }

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String extractCultureFromPath(Path rule) {

        Path current = Paths.get(rule.toString());
        while (current != rule.getRoot()) {
            if (availableCultures.contains(current.getFileName().toString())) return current.getFileName().toString();
            current = current.getParent();
        }
        return null;
    }

}
