package skycubedPlatformer.assets.triggers;

import java.awt.Graphics;

import skycubedPlatformer.assets.DecorationObject;
import skycubedPlatformer.assets.Trigger;
import skycubedPlatformer.game.ObjType;
import skycubedPlatformer.game.Player;
import skycubedPlatformer.menu.ApplicationFrame;
import skycubedPlatformer.menu.GamePanel;

public class TextDisplayTrigger extends Trigger {

	public String text;
	public int displayTime;
	
	public TextDisplayTrigger(double x, double y, double size_x, double size_y, String text, int displayTime) {
		super(x, y, size_x, size_y, null);
		
		this.text = text;
		this.displayTime = displayTime;
	}
	
	@Override
	public void run() {
		super.run();
		GamePanel.getPanel().displayText(this.text, this.displayTime);
	}
	
	@Override
	public void draw(Graphics g, Player player, double cam_x, double cam_y, double size) {
		//don't draw
	}

}
