package platformerTest.appdata;

import java.io.File;
import java.io.FileWriter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataManager {
	public static ObjectMapper mapper = new ObjectMapper();
	public static SaveData saveData;
	
	public static void onStart() {
		if (FileLoader.saveFile == null) return;
		File save = FileLoader.saveFile;
		
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		try {
			saveData = mapper.readValue(save, SaveData.class);
			
		} catch (Exception e) {
			saveData = new SaveData();
			e.printStackTrace();
		}
		
	}
	
	public static void save() {
		if (FileLoader.saveFile == null) return;
		File save = FileLoader.saveFile;
		
		try {
			String saveValue = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(saveData);
			FileWriter writer = new FileWriter(save);
			
			writer.write(saveValue);
			
			writer.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}