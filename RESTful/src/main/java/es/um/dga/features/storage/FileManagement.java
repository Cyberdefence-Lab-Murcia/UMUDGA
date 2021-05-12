/**
 * Project: CollisionChecker
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.storage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

public class FileManagement {
    public static List<File> getDirectories(File rootDirectory) {
        File[] directories = rootDirectory.listFiles(new FilenameFilter() {
            @Override public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        assert directories != null;
        return Arrays.asList(directories);
    }
    
    public static List<File> getFiles(File rootDirectory) {
        File[] files = rootDirectory.listFiles(new FilenameFilter() {
            @Override public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
        assert files != null;
        return Arrays.asList(files);
    }
}
