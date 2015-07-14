package com.github.eugene.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class AppConfig {
    
    public static void getDirs() {
        
        Path path = Paths.get("foo", "bar", "baz.txt");
        System.out.println(path);
        String buildDirPath = "C:\\.jenkins\\jobs\\Connect Automated Functional Tests - Firefox 32\\builds";
        
        File dir = new File(buildDirPath);
        File[] buildsDir = dir.listFiles();

    }

}
