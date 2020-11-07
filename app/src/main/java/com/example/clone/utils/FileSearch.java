package com.example.clone.utils;

import java.io.File;
import java.util.ArrayList;

public class FileSearch {

    // search a directory and return a list of all directories contained inside

    public static ArrayList<String>getDirectoryPaths(String directory){
        ArrayList<String>pathArray=new ArrayList<>();
        File file= new File(directory);
        File[] listfiles=file.listFiles();
        for (int i=0; i<listfiles.length;i++){
            if(listfiles[i].isDirectory()){
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

    // search a directory and return a list of all file contained inside

    public static ArrayList<String>getFilePaths(String directory){
        ArrayList<String>pathArray=new ArrayList<>();
        File file= new File(directory);
        File[] listfiles=file.listFiles();
        for (int i=0; i<listfiles.length;i++){
            if(listfiles[i].isFile()){
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }
}
