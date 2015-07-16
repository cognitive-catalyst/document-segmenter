package com.ibm.watson.ecosystem.text_segmenter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.util.Properties;

public class Segmenter {
    public Properties prop;
	int textCounter, paragraphCount;
	int minimumSlice;
	
	public void segment(File f, PrintWriter pw) throws IOException{
		Document doc = Jsoup.parse(f, "UTF-8", "");
		Elements hTags = doc.select("h1, h2, h3, h4, h5, h6");
		for(int i = 0; i < hTags.size(); ++i){
			sectionExaminer(hTags.get(i));
		}
		pw.println(doc);
		pw.flush();
	}
	
	public void sectionExaminer(Node n){
		getSectionLength(n, 0);
		if(textCounter > Integer.parseInt(prop.getProperty("sliceSize"))){
			paragraphCount = 0;
			tryToSlice(n, n, n, n, 0, 0, textCounter);
		};
	}
	
	public int getSectionLength(Node start, int depth){
		if(depth == 0)
			textCounter = 0;
		for(Node loop = start.nextSibling(); loop != null; loop = loop.nextSibling()){
			if(loop.nodeName().equals("p") || loop.nodeName().equals("#text")){
				textCounter += loop.outerHtml().replaceAll("\\<[^>]*>", "").length();
			} else if(loop.nodeName().equals("#text")){
				textCounter += loop.outerHtml().replaceAll("\\<[^>]*>", "").length();
			} else if(loop.nodeName().matches("h[123456]")){
				return -1;
			}
			else {
			    List<Node> nl = loop.childNodes();
			    if(!nl.isEmpty())
			    	if(getSectionLength(nl.get(0),depth+1) == -1)
			    		return -1;
			}
		}
		return textCounter;
	}
	
	public int getNodeLength(Node start, int depth){
		if(depth == 0)
			textCounter = 0;
		if(start.nodeName().equals("p") || start.nodeName().equals("#text")){
			textCounter += start.outerHtml().replaceAll("\\<[^>]*>", "").length();
		} else if(start.nodeName().matches("h[123456]")){
			return -1;
		} else {
			List<Node> nl = start.childNodes();
			if(!nl.isEmpty())
				if(getSectionLength(nl.get(0),depth+1) == -1)
					return -1;
			}
		return textCounter;
	}
	
	public int tryToSlice(Node head, Node n, Node prevSliceLoc, Node curSliceLoc, int prevLength, int curLength, int totalLength){
		//start a cumulative count of section length
		for(Node loop = n.nextSibling(); loop != null; loop = loop.nextSibling()){
			if(loop.nodeName().equals("p") || loop.nodeName().equals("#text")){
				getNodeLength(loop, 0);
				if(textCounter > 1){
					prevLength = curLength;
					curLength += textCounter;
					prevSliceLoc = curSliceLoc;
					curSliceLoc = loop;
					++paragraphCount;
				}
			} else if (loop.nodeName().equals("table") || loop.nodeName().equals("ul") || 
					loop.nodeName().equals("dl") || loop.nodeName().equals("ol")){
				getNodeLength(loop, 0);
				if(textCounter > 1){
					prevLength = curLength;
					curLength += textCounter;
					prevSliceLoc = curSliceLoc;
					curSliceLoc = loop;
					++paragraphCount;
				}
			} else {			
				List<Node> nl = loop.childNodes();
			    if(!nl.isEmpty())
			    	curLength = tryToSlice(head, nl.get(0), prevSliceLoc, curSliceLoc, prevLength, curLength, totalLength);
			}
			if(curLength == -1)
				return curLength;
			if(curLength >= totalLength/2){
				if((totalLength/2 - prevLength) < (curLength - totalLength/2)){
					curSliceLoc = prevSliceLoc;
					curLength = prevLength;
				}
				//The 5 here is because of a bug where curLength and totalLength aren't quite calculated 
				//identically. needs to be fixed or may cause rare errors
				if(curSliceLoc != head && curLength < totalLength-5 
						&& minimumSlice <= curLength
						&& minimumSlice <= totalLength-curLength){
					curSliceLoc.after("<h6>Watson Segmentation Header</h6>");
					sectionExaminer(head);
					sectionExaminer(curSliceLoc.nextSibling());
				}
				return -1;
			}
		}
		return curLength;
	}
	private void loadProperties(){
		try {
			String filename = "config.properties";
			InputStream input = new FileInputStream(filename);
			prop.load(input);
			input.close();
			minimumSlice = Integer.parseInt(prop.getProperty("minimumSectionSize"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
		  
	Segmenter(){
		prop = new Properties();
		loadProperties();
	}
}
