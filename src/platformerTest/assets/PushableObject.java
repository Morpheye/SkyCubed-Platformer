package platformerTest.assets;

import java.awt.Color;
import java.util.ArrayList;

import platformerTest.game.GameObject;
import platformerTest.game.ObjType;
import platformerTest.menu.GamePanel;

public class PushableObject extends MovableObject {

	public PushableObject(double x, double y, double size_x, double size_y, Color color, double density, Double slipperiness) {
		super(x, y, size_x, size_y, color, density);
		this.slipperiness = slipperiness;
	}
	
	@Override
	public void move() {
		
		this.inLiquid = false;
		
		for (GameObject obj : GamePanel.objects) { //check for water
			if (obj.equals(this)) continue;
			if (this.hasCollided(obj) && obj.type.equals(ObjType.LiquidPlatform) && obj.exists) {
				this.inLiquid = true;
				this.liquidDensity = ((LiquidPlatform) obj).density;
			}
		}
		
		if (this.inLiquid) {
			double diff = this.density - liquidDensity;
			double lift = GamePanel.gravity * Math.atan(2*diff) / (Math.PI / 2) - GamePanel.gravity;
			
			this.vy += lift;
			
			this.attemptMoveY(lift, false);
		}
		
		ArrayList<GameObject> collisions = new ArrayList<GameObject>();
		boolean collidedy = false;
		
		super.move();
	}

}
