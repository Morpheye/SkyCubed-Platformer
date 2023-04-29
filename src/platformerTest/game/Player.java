package platformerTest.game;

import java.awt.Color;
import java.awt.Graphics;

import platformerTest.assets.LiquidPlatform;
import platformerTest.assets.MovableObject;
import platformerTest.assets.Trigger;
import platformerTest.assets.triggers.Powerup;
import platformerTest.menu.GamePanel;

public class Player extends MovableObject {

	public double movementSpeed = 0.25;
	public double jumpStrength = 16;
	
	public int health;
	public boolean isAlive;
	
	boolean movingInLiquid = false;
	
	public Player(double initX, double initY, double size) {
		super(initX, initY, size, size, Color.WHITE, 1.0);
		
		this.type = ObjType.Player;
		
		this.movable = true;
		this.x = initX;
		this.y = initY;
		this.size_x = size;
		this.size_y = size;
		this.slipperiness = 1;
		this.health = 100;
		this.isAlive = true;
		
	}
	
	public boolean movingUp = false;
	public boolean movingDown = false;
	public boolean movingLeft = false;
	public boolean movingRight = false;
	
	@Override
	public void move() {
		
		this.movingInLiquid = false;
		this.liquidDensity = 1;
		
		for (GameObject obj : GamePanel.objects) { //check for water
			if (obj.equals(this)) continue;
			//finish flag
			if (obj.hasCollided(this) && obj.type.equals(ObjType.FinishFlag) && GamePanel.levelWon==0 && obj.exists) {
				GamePanel.levelWon=1;
			}
			//powerup
			if (obj.hasCollided(this) && obj.type.equals(ObjType.Powerup) && GamePanel.levelWon==0 && obj.exists) {
				((Powerup) obj).run();
				obj.exists = false;
			}
			//trigger
			if (obj.hasCollided(this) && obj.type.equals(ObjType.Trigger) && GamePanel.levelWon==0 && obj.exists) {
				((Trigger) obj).run();
				obj.exists = false;
			}
			//liquids
			if (this.hasCollided(obj) && obj.type.equals(ObjType.LiquidPlatform) && obj.exists) {
				this.movingInLiquid = true;
				this.liquidDensity = ((LiquidPlatform) obj).density;
			}
		}
		
		if (this.inLiquid) {
			double diff = this.density - liquidDensity;
			double lift = GamePanel.gravity * Math.atan(2*diff) / (Math.PI / 2) - GamePanel.gravity;
			
			this.vy += lift;

		}
		
		if (movingInLiquid) {
			double slowdown = 1;
			double diff = this.density - liquidDensity;
			if (diff > 1) {
				slowdown = 1/(diff+0.5); //starts at 1, approaches 0
			}
			
			if (this.movingUp) this.vy += Math.pow(1, slowdown)*this.movementSpeed;
			if (this.movingDown)this.vy -= Math.pow(1, slowdown)*this.movementSpeed;
			if (this.movingRight) this.vx += Math.pow(1, slowdown)*this.movementSpeed;
			if (this.movingLeft) this.vx -= Math.pow(1, slowdown)*this.movementSpeed;
			
		} else {
			if (this.movingUp & !this.inAir) { //jump
				this.vy += this.jumpStrength;
				this.inAir = true;
			}
			if (this.movingRight) this.vx += this.movementSpeed;
			if (this.movingLeft) this.vx -= this.movementSpeed;
		}
		
		if (this.y > GamePanel.level.topLimit && GamePanel.levelWon==0) GamePanel.restartLevel(GamePanel.level);
		if (this.y < GamePanel.level.bottomLimit && GamePanel.levelWon==0) GamePanel.restartLevel(GamePanel.level);
		
		if (this.health <= 0) this.die();
		
		//then apply movableobject physics
		super.move();
		
		
	}
	
	@Override
	public void draw(Graphics g, Player player, double x, double y, double size) {
		super.draw(g, player, x, y, size);
	}
	
	public void damage(int damage) {
		this.health -= damage;
		if (this.health <= 0) this.die();
	}
	
	@Override
	public void crush() {
		if (GamePanel.levelWon == 0) {
			this.health = 0;
			this.die();
		}
	}
	
	@Override
	public void die() {
		if (GamePanel.levelWon == 0 && this.isAlive) {
			GamePanel.createFlash(Color.BLACK, 100);
			this.isAlive = false;
			this.exists = false;
		}
	}
	

	
}
