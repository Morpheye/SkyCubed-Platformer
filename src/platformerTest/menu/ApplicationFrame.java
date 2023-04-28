package platformerTest.menu;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import platformerTest.Main;
import platformerTest.levels.Level;
import platformerTest.levels.world1.World1;

@SuppressWarnings("serial")
public class ApplicationFrame extends JFrame {

	public static Component current;
	
	public ApplicationFrame() {
		this.setTitle("Platformer Test");
		this.setSize(Main.SIZE,Main.SIZE);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosed(WindowEvent e) {
	        	System.exit(0);
	        }
	    });
		
		Container panel = this.getContentPane();
		panel.add(new MenuPanel(new World1()));
		
		this.setVisible(true);
	}
	
	public void startGame(Level level) {
		Container panel = this.getContentPane();
		panel.removeAll();
		
		if (current != null) current.setEnabled(false);
		current = new GamePanel(level);
		panel.add(current);
		current.requestFocus();	
	}
	
	public void exitGame(Level level) {
		Container panel = this.getContentPane();
		panel.removeAll();
		
		if (current != null) current.setEnabled(false);
		current = new MenuPanel(new World1());
		panel.add(current);
		current.requestFocus();
	}
	
}