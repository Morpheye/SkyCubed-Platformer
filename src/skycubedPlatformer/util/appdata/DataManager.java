package skycubedPlatformer.util.appdata;

import java.io.File;
import java.io.FileWriter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import skycubedPlatformer.items.Item;
import skycubedPlatformer.items.weapons.Weapon;
import skycubedPlatformer.levels.LevelWorld;
import skycubedPlatformer.util.ImageHelper;

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
			System.out.println("Nonexisting or invalid save data. Creating new...");
		}
		
		Item.itemListInit();
		LevelWorld.init();
		(new ImageHelper()).init();
		
	}
	
	public static void save() {
		if (FileLoader.saveFile == null) return;
		File save = FileLoader.saveFile;
		
		try {
			if (saveData.gems < 0) saveData.gems = Long.MAX_VALUE;
			if (saveData.coins < 0) saveData.coins = Long.MAX_VALUE;
			
			String saveValue = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(saveData);
			FileWriter writer = new FileWriter(save);
			
			writer.write(saveValue);
			
			writer.close();
			
		} catch (Exception e) {
			System.out.println("ERROR: Failed to save progress.");
		}
		
	}
	
	public static void addItem(String item, long amount) {
		Long amt = DataManager.saveData.inventory.get(item);
		if (amt == null) amt = 0L;
		DataManager.saveData.inventory.put(item, amt + 1);
	}
	
}
