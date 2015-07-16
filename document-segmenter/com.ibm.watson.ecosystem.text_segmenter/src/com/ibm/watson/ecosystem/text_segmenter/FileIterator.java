package com.ibm.watson.ecosystem.text_segmenter;

import java.io.File;
import java.io.PrintWriter;

public class FileIterator {
  public static void main(String[] args){
    Segmenter seg = new Segmenter();

    File dir = new File("./html/source");
    File[] fileList = dir.listFiles();
    if(fileList != null) {
      for(File current: fileList){
        String filename = current.getName();
        int pos = filename.lastIndexOf(".");
        if(pos > 0)
          filename = filename.substring(0, pos);
        System.out.println(filename);
        try{
          PrintWriter writer = new PrintWriter("./html/segmented/"+filename+".html", "UTF-8");
          seg.segment(current, writer);
        } catch (Exception e){
        }
      }
    } else {
      System.err.println("Error: ./xml directory is empty");
    }
  }
}
