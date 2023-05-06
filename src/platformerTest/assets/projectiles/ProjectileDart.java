package platformerTest.assets.projectiles;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import platformerTest.Main;
import platformerTest.assets.Projectile;
import platformerTest.game.GameObject;
import platformerTest.game.Player;
import platformerTest.menu.GamePanel;

public class ProjectileDart extends Projectile {

	public InputStream inputHit = new BufferedInputStream(this.getClass().getResourceAsStream("/sounds/attack/default/thud.wav"));
	
	public ProjectileDart(double x, double y, double vx, double vy, GameObject firer, int damage) {
		super(x, y, 10, 10, vx, vy, firer, damage);
		this.lifetime = 300;
		this.waterResistant = true;
		this.pushStrength = 1;
		this.rotating = true;
		
		try {
			AudioInputStream audioStreamHit = AudioSystem.getAudioInputStream(inputHit);
			
			this.hitSound = AudioSystem.getClip();
			this.hitSound.open(audioStreamHit);
		
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@Override
	public void move() {
		this.vy += GamePanel.gravity * 0.05;
		
		super.move();
		
	}
	
	@Override
	public void draw(Graphics g, Player player, double cam_x, double cam_y, double size) {
		int drawX = (int) ( (this.x - (cam_x - size/2)) * (Main.SIZE/size));
		int drawY = (int) ( (size - (this.y) + (cam_y - size/2)) * (Main.SIZE/size));
		
		int direction = (vx > 0) ? 1 : -1;
		int angle = -90;
		if (this.rotating) {
			angle = (int) (Math.atan(this.vy/this.vx) * 180 / Math.PI);
			if (direction == -1) angle = 180 + angle;
		}
		Graphics2D g2d = (Graphics2D) g;
		
		double p1OffsetX = (double) (Math.cos(angle * Math.PI/180) * this.size_x * (Main.SIZE/size));
		double p1OffsetY = (double) (Math.sin(angle * Math.PI/180) * this.size_y * (Main.SIZE/size));
		
		double p2OffsetX = (double) -(Math.cos(angle * Math.PI/180) * 0.75 * this.size_x * (Main.SIZE/size));
		double p2OffsetY = (double) -(Math.sin(angle * Math.PI/180) * 0.75 * this.size_y * (Main.SIZE/size));
		
		double p3OffsetX = (double) -(Math.cos(angle * Math.PI/180) * 0.5 * this.size_x * (Main.SIZE/size));
		double p3OffsetY = (double) -(Math.sin(angle * Math.PI/180) * 0.5 * this.size_y * (Main.SIZE/size));
		
		g2d.setStroke(new BasicStroke((float)(6*(Main.SIZE/size))));
		g2d.setColor(GameObject.COLOR_WOOD.darker().darker());
		g2d.drawLine(drawX+(int)p3OffsetX, drawY-(int)p3OffsetY, drawX+(int)p2OffsetX, drawY-(int)p2OffsetY);
		
		g2d.setStroke(new BasicStroke((float)(4*(Main.SIZE/size))));
		g2d.setColor(GameObject.COLOR_WOOD.darker());
		g2d.drawLine(drawX+(int)p1OffsetX, drawY-(int)p1OffsetY, drawX+(int)p2OffsetX, drawY-(int)p2OffsetY);
		
	}

}
