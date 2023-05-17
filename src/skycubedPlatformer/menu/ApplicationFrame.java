package skycubedPlatformer.menu;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import skycubedPlatformer.Main;
import skycubedPlatformer.appdata.DataManager;
import skycubedPlatformer.appdata.discord.DiscordRPC;
import skycubedPlatformer.levels.Level;
import skycubedPlatformer.levels.LevelWorld;
import skycubedPlatformer.levels.world1.World1;
import skycubedPlatformer.weapons.Weapon;

@SuppressWarnings("serial")
public class ApplicationFrame extends JFrame {

	public static Component current;
	
	public ApplicationFrame() {
		this.setTitle("SkyCubed Platformer");
		this.setSize(Main.SIZE,Main.SIZE);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		try {
		this.setIconImage(ImageIO.read(this.getClass().getResource("/icon.png")));
		} catch (Exception e) {}
		this.addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosed(WindowEvent e) { //finish up
	        	DataManager.save();
	        	System.exit(0);
	        }
	    });
		
		Container panel = this.getContentPane();
		panel.add(new MainPanel());
		
		this.setVisible(true);
	}
	
	public void startGame(Level level) {
		Container panel = this.getContentPane();
		panel.removeAll();
		
		System.gc();
		
		if (current != null) current.setEnabled(false);
		current = new GamePanel(level);
		panel.add(current);
		current.requestFocus();	
		
		String wielding = (Weapon.getWeapon(DataManager.saveData.selectedWeapon) != null) ?
				"Wielding " + DataManager.saveData.selectedWeapon : "Empty-handed";
		DiscordRPC.updateStatus(level.getClass().getSimpleName() + ": " + level.name, wielding);
	}
	
	public void openLevelSelect(Level level) {
		Container panel = this.getContentPane();
		panel.removeAll();
		DataManager.save();
		
		System.gc();
		
		if (current != null) current.setEnabled(false);
		
		current = new MenuPanel();
		panel.add(current);
		current.requestFocus();
		DiscordRPC.updateStatus("In Level Select", "");
	}
	
	public void openWeaponsMenu() {
		Container panel = this.getContentPane();
		panel.removeAll();
		DataManager.save();
		
		System.gc();
		
		if (current != null) current.setEnabled(false);
		current = new WeaponsPanel();
		panel.add(current);
		current.requestFocus();
		
		String wielding = (Weapon.getWeapon(DataManager.saveData.selectedWeapon) != null) ?
				"Wielding " + DataManager.saveData.selectedWeapon : "Empty-handed";
		DiscordRPC.updateStatus("In Weapons Menu", wielding);
	}
	
	public void openMainMenu() {
		Container panel = this.getContentPane();
		panel.removeAll();
		DataManager.save();
		
		System.gc();
		
		if (current != null) current.setEnabled(false);
		current = new MainPanel();
		panel.add(current);
		current.requestFocus();
		DiscordRPC.updateStatus("In Main Menu", "");
	}
	
}