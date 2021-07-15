package qualityACR;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

public class GeneraReport {
	
	
	 void readLargerTextFileAlternate(String fileName) throws IOException {
		    Path path = Paths.get(fileName);
		    try (BufferedReader reader = Files.newBufferedReader(path)){
		      String line = null;
		      while ((line = reader.readLine()) != null) {
		        //process each line in some way
		      }      
		    }
		  }
		
	 void writeLargerTextFile(String fileName, List<String> lines) throws IOException {
		    Path path = Paths.get(fileName);
		    try (BufferedWriter writer = Files.newBufferedWriter(path)){
		      for(String line : lines){
		        writer.write(line);
		        writer.newLine();
		      }
		    }
		  }	 
		
		
		
		
		
	}
