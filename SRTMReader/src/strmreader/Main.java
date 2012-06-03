package strmreader;

import java.io.File;
import java.io.FileWriter;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath = "";
		String outputFile = "";
		File file = null;
		SRTMReader reader;
		int precision = 3; // ca 100m
		String type = "";
		String variableName = "";
		
		if (args.length > 0) {
			filePath = args[0];
		}
		
		if (args.length > 1) {
			type = args[1];
		}
		
		if (args.length > 2) {
			variableName = args[2];
		}
		
		if (filePath.isEmpty()) {
			System.err.println("Error: No file specified");
			System.exit(0);
		}
		else {
			file = new File(filePath);
		}
		
		if (file.exists() && file.canRead()) {
			System.out.println("Reading file: " + filePath);
			
			reader = new SRTMReader();
			reader.init(SRTMReader.SRTM3);
			reader.read(file);
			
			int[][] heights = reader.getHeightArray();
			
			Generator generator = Generator.createGenerator(type);
			generator.generateMesh(heights,
					precision,
					320, 
					420, 
					1040, 
					1140);
			String json = generator.toJSON(variableName);
			
			try{
				FileWriter writer = new FileWriter(filePath.replaceAll(".hgt", "." + generator.getMeshType() +  ".json"));
				writer.write(json);
				writer.close();
				System.out.println("JSON file created");
			} 
			catch(Exception ex) {
				System.out.println("cannot write json");
			}
		}
		else{
			System.err.println("Error reading file: " + filePath + ". Does not exist, or is not readable.");
		}
	}

}
