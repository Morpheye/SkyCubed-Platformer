package skycubedPlatformer.assets;

import java.awt.Color;
import java.awt.Graphics;

import skycubedPlatformer.Main;
import skycubedPlatformer.game.GameObject;
import skycubedPlatformer.game.ObjType;
import skycubedPlatformer.game.Player;

public class LiquidPlatform extends GameObject {
	
	public double slipperiness = 0.6;
	public LiquidType liquidType;

	public LiquidPlatform(double x, double y, double size_x, double size_y, Color color) {
		super(x, y, size_x, size_y, color);
		
		this.type = ObjType.LiquidPlatform;
		
		this.movable = false;
		this.solid = false;
	}
	
	@Override
	public void draw(Graphics g, Player player, double x, double y, double size) {
		super.draw(g, player, x, y, size);
	}
	
	@Override
	public void move() {
		this.y += this.vy;
		this.x += this.vx;
	}

	public void onTick(GameObject obj) {
		
	}
	
	public static enum LiquidType {
		Water,
		Lava
	}
	
}