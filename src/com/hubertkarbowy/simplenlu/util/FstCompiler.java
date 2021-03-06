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
            rules = Files.walk(fstRootPath).filter(x -> (x.toString().contains("/root") | x.toString().contains("/contextual"))
                                                        && x.toString().endsWith(".txt")
                                                        && !x.toString().contains("_isyms")
                                                        && !x.toString().contains("_osyms")).collect(Collectors.toList());

            for (Path rule : rules) {
                Path basename = rule.getFileName();
                String culture = extractCultureFromPath(rule);

                Path compiledFst = rule.getParent().resolve(basename.toString().replaceAll(".txt", ".fst"));
                System.out.println("Compiling " + rule + " into " + compiledFst + " [culture = " + culture + "]");
                String[] cmd;
                if (!rule.toString().contains("_special/") & !rule.toString().contains("contextual/")) {
                    String[] zz = {"/bin/sh", "-c", "fstcompile --isymbols=" + fstRootPath + "/" + culture + "/isyms.txt --osymbols=" + fstRootPath + "/" + culture + "/osyms.txt --keep_isymbols --keep_osymbols " + rule + " " + compiledFst};
                    cmd = zz;
                }
                else {
                    // 1. extract final _something.txt
                    // 2. find same i/osyms in special dir
                    String specialRuleIdentifier = rule.toString().replaceAll(".txt$", "");
                    String[] zz = {"/bin/sh", "-c", "fstcompile --isymbols=" + specialRuleIdentifier + "_isyms.txt --osymbols=" + specialRuleIdentifier + "_osyms.txt --keep_isymbols --keep_osymbols " + rule + " " + compiledFst};
                    cmd = zz;
                }
                System.out.println(String.join(" ", cmd));
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
