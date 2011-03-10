package fr.lasconic.nwc2musicxml.convert.batch;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fr.lasconic.nwc2musicxml.convert.Nwc2MusicXML;

public class Nwc2mscz {
	
	public static final String NWC2TXT = "nwc2txt";
	public static final String MUSESCORE = "C:/Program Files/MuseScore/bin/mscore";
	
	public static void main(String[] args) {
		String path = ".";
		if (args.length == 1) {
			path = args[0];
		}else {
			System.err.println("please provide a directory or file path");
		}
			
		File dir = new File(path);
		convertDir(dir);
	}

	private static void convertDir(File dir) {
		File[] files = dir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				return pathname.getName().toLowerCase().endsWith(".nwc") || pathname.isDirectory();
			}});
		
		for (int i = 0; i < files.length; i++) {
			File nwcFile = files[i];
			if(nwcFile.isFile()){
				convertFile(dir, nwcFile);
			}else if (nwcFile.isDirectory()){
				convertDir(nwcFile);
			}else{	
				System.out.println("Error");
			}
		}
	}

	private static void convertFile(File dir, File nwcFile) {
		String filename = nwcFile.getName();
		String name = filename.substring(0,filename.lastIndexOf("."));
		
		//run nwc2txt from noteworthy software
		File nwcTxtFile = new File(dir, name+".nwctxt");
		ProcessBuilder pb = new ProcessBuilder(NWC2TXT, "\""+ nwcFile.getPath() +"\"",  "\""+ nwcTxtFile.getAbsolutePath() +"\"");
		try {
			Process p = pb.start();
			p.waitFor();
			
			if(nwcTxtFile.exists()) {
				//run txt 2 musicxml
				File musicXmlFile = new File(dir, name+".xml");
				FileInputStream fileInputStream = new FileInputStream(nwcTxtFile);
				Nwc2MusicXML converter = new Nwc2MusicXML();
				String title = converter.convert(fileInputStream);
				System.out.println("Converting... title: [" + title + "]");
				if (converter.write(new FileOutputStream(musicXmlFile)) == -1) {
					System.out.println("Error while converting [" + title
							+ "]");
				} else {
					System.out.println("Success !  ["
							+ musicXmlFile.getAbsolutePath() + "]");
					//run MuseScore to do MusicXML to mscz
					File msczFile = new File(dir, name + ".mscz");
					ProcessBuilder pbMscz = new ProcessBuilder(MUSESCORE, "\""+ musicXmlFile.getPath() +"\"", "-o" ,"\""+ msczFile.getAbsolutePath() +"\"");
					Process pMscz = pbMscz.start();
					int ret = pMscz.waitFor();
					System.out.println(ret + " : " + msczFile.getAbsolutePath() );
					//Thread.sleep(1000);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
