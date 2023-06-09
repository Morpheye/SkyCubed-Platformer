package skycubedPlatformer.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import skycubedPlatformer.Main;
import skycubedPlatformer.assets.Trigger;
import skycubedPlatformer.assets.decoration.particles.CoinParticle;
import skycubedPlatformer.assets.effects.Effect;
import skycubedPlatformer.assets.triggers.Powerup;
import skycubedPlatformer.items.weapons.Weapon;
import skycubedPlatformer.menu.ApplicationFrame;
import skycubedPlatformer.menu.GamePanel;
import skycubedPlatformer.util.SoundHelper;
import skycubedPlatformer.util.appdata.DataManager;

public class Player extends LivingObject {
	
	Clip finishSound;
	public int naturalRegenCooldown;
	public double luck;
	
	public Player(double initX, double initY, double size) {
		super(initX, initY, size, size, Color.WHITE, 1.0);	
		this.type = ObjType.Player;
		this.luck = 0;
		
		this.movementSpeed = 0.25;
		this.jumpStrength = 16;
		
		this.movable = true;
		this.x = initX;
		this.y = initY;
		this.size_x = size;
		this.size_y = size;
		this.slipperiness = 1;
		this.isAlive = true;
		this.density = 1;
		
		//combat
		this.dmgTime = 0;
		
		this.maxHealth = 100;
		this.health = this.maxHealth;
		this.naturalRegenCooldown = 180;
		this.overheal = 0;
		this.timeSinceDamaged = 0;
		this.timeSinceDeath = 0;
		this.fireResistant = false;
		
		this.maxAttackCooldown = 40;
		this.attackCooldown = 0;
		this.attackRange = 20;
		this.attackDamage = 5;
		this.rangedAttackDamage = 5;
		this.attackKnockback = 2;
		
		this.weapon = Weapon.getWeapon(DataManager.saveData.selectedWeapon);
		
		String inputAttack;
		String inputHit;
		
		if (this.weapon != null) { //sounds
			this.weapon.init(this);
			if (this.weapon.attackSound != null) inputAttack = this.weapon.attackSound;
			else inputAttack = "/sounds/attack/default/attack.wav";
			
			if (this.weapon.hitSound != null) inputHit = this.weapon.hitSound;
			else inputHit = "/sounds/attack/default/hit.wav";
			
		} else {
			inputAttack = "/sounds/attack/default/attack.wav";
			inputHit = "/sounds/attack/default/hit.wav";
		}
		
		String finish = "/sounds/finish.wav";
		
		try {
			this.attackSound = AudioSystem.getClip();
			this.hitSound = AudioSystem.getClip();
			this.finishSound = AudioSystem.getClip();
			SoundHelper.loadSound(this, this.attackSound, inputAttack);
			SoundHelper.loadSound(this, this.hitSound, inputHit);
			SoundHelper.loadSound(this, this.finishSound, finish);
			
		} catch (Exception e) {e.printStackTrace();}
		
	}
	
	@Override
	public void move() {
		//Check interactables 
		for (GameObject obj : GamePanel.getPanel().objects) { //check for water
			if (obj.equals(this)) continue;
			//finish flag
			if (obj.hasCollided(this) && obj.type.equals(ObjType.FinishFlag) 
					&& GamePanel.getPanel().levelWon==0 && obj.exists) {
				GamePanel.getPanel().levelWon=1;
				SoundHelper.playFinalSound(finishSound, Main.VOLUME);
				CoinParticle.spawnCoins(this.x, this.y, 5+(int)(Math.random() * 6), GamePanel.getPanel().level.reward);
			}
			//null zone
			if (obj.hasCollided(this) && obj.type.equals(ObjType.NullZone) 
					&& GamePanel.getPanel().levelWon==0 && obj.exists) {
				this.maxHealth = 100; this.movementSpeed = 0.25; this.jumpStrength = 16;
				this.fireResistant = false; this.naturalRegenCooldown = 180; this.overheal = 0;
				this.maxAttackCooldown = 40; this.attackRange = 20;
				this.attackDamage = 5; this.rangedAttackDamage = 5; this.attackKnockback = 2;
				this.density = 1; GamePanel.getPanel().target_camera_size = 800;
				this.luck = 0;
				
				if (this.weapon != null) this.weapon.init(this);
			}
			//powerup
			if (obj.hasCollided(this) && obj.type.equals(ObjType.Powerup) 
					&& GamePanel.getPanel().levelWon==0 && obj.exists) {
				((Powerup) obj).run();
				((Powerup) obj).playSound();
				obj.exists = false;
			}
			//trigger
			if (obj.hasCollided(this) && obj.type.equals(ObjType.Trigger) 
					&& GamePanel.getPanel().levelWon==0 && obj.exists) {
				((Trigger) obj).run();
				obj.exists = false;
			}
		}
		
		super.move();
		
		//Health
		if (timeSinceDamaged >= 450) {
			this.timeSinceDamaged -= naturalRegenCooldown;
			if (this.health < this.maxHealth) this.health++;
		}
		this.timeSinceDamaged++;
		
		//CHECK BOUNDS
		if (this.y > GamePanel.getPanel().level.topLimit && GamePanel.getPanel().levelWon==0) {
			GamePanel.getPanel().restartLevel(GamePanel.getPanel().level);
		}
		if (this.y < GamePanel.getPanel().level.bottomLimit && GamePanel.getPanel().levelWon==0) {
			GamePanel.getPanel().restartLevel(GamePanel.getPanel().level);
		}
		if (this.timeSinceDeath > 120 && GamePanel.getPanel().levelWon==0) {
			GamePanel.getPanel().restartLevel(GamePanel.getPanel().level);
		}

	}
	
	@Override
	public void draw(Graphics g, Player player, double x, double y, double size) {
		int alpha = (this.timeSinceDeath > 120) ? 0 : 255 - (this.timeSinceDeath * 2);
		int drawX = (int) ( (this.x - (this.size_x)/2 - (x - size/2)) * (Main.SIZE/size));
		int drawY = (int) ( (size - (this.y + (this.size_y)/2) + (y - size/2)) * (Main.SIZE/size));
		//WHITE and modify opacity for deathtime
		g.setColor(new Color(this.color.getRed(),this.color.getGreen(),this.color.getBlue(),alpha));
		g.fillRoundRect(drawX, drawY, (int) (this.size_x * Main.SIZE/size), (int) (this.size_y * Main.SIZE/size), 
		(int)(5*(Main.SIZE/size)), (int)(5*(Main.SIZE/size)));
		
		//eyes
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke((float)(4*(Main.SIZE/size))));
		g2d.setColor(new Color(0,0,0,255-(2*this.timeSinceDeath))); //BLACK and modify opacity for deathtime
		int x1, x2;
		if (this.lastDirection == 1) {
			x1 = (int) (drawX+(this.size_x*(Main.SIZE/size)*2/5));
			x2 = (int) (drawX+(this.size_x*(Main.SIZE/size)*3/4));
		} else {
			x1 = (int) (drawX+(this.size_x*(Main.SIZE/size)*1/4));
			x2 = (int) (drawX+(this.size_x*(Main.SIZE/size)*3/5));
		}
		int y1 = (int) (drawY+(this.size_y*(Main.SIZE/size)*1/4));
		int y2 = (int) (drawY+(this.size_y*(Main.SIZE/size)*2/4));
		g2d.drawLine(x1, y1, x1, y2);
		g2d.drawLine(x2, y1, x2, y2);
		
		//damage
		if (this.dmgTime != 0) {
			if (this.overheal > 100) g.setColor(new Color(0, 255, 255, this.dmgTime));
			else if (this.overheal > 0) g.setColor(new Color(255, 215, 0, this.dmgTime));
			else if (this.lastDamageEffect != null) Effect.getEffectColor(g2d, lastDamageEffect, this.dmgTime);
			else g.setColor(new Color(255, 0, 0, this.dmgTime));
			
			g.fillRoundRect(drawX, drawY, (int) (this.size_x * Main.SIZE/size), (int) (this.size_y * Main.SIZE/size), 
			(int)(5*(Main.SIZE/size)), (int)(5*(Main.SIZE/size)));
		}
		
		if (this.dmgTime > 0) this.dmgTime -= 5;
		
	}
	
	@Override
	public void damage(int damage, GameObject source) {
		super.damage(damage, source);
		GamePanel.getPanel().createShake(3, 40 * (double) damage / this.maxHealth, 2);
	}
	
	@Override
	public void rangedAttack() {
		if (!this.isAlive) return;
		
		int calcX = 0, calcY = 0, angle = 0;
		if (this.movingUp) calcY++;
		if (this.movingDown) calcY--;
		if (this.movingRight || this.movingLeft) calcX += this.lastDirection;
		if (calcX == 0 && calcY == 0) calcX += this.lastDirection; //no input
		if (calcX == 0) angle = 90 * calcY;
		
		if (calcX == 0) angle = 90 * calcY;
		else if (calcY == 0) angle = 90 - (90*calcX);
		else if (calcY == 1) angle = 90 - (45*calcX);
		else if (calcY == -1) angle = -90 + (45*calcX);
		
		if (this.weapon != null) this.weapon.rangedAttack(this, angle);
		this.attackCooldown = this.maxAttackCooldown;
	}
	
	@Override
	public void crush() {
		if (GamePanel.getPanel().levelWon == 0) {
			this.health = 0;
			this.die();
		}
	}
	
	@Override
	public void die() {
		if (GamePanel.getPanel().levelWon == 0 && this.isAlive) {
			GamePanel.getPanel().createFlash(Color.BLACK, 150);
			GamePanel.getPanel().createShake(30, 15);
			this.removeEffects.addAll(this.effects);
			this.isAlive = false;
			this.exists = false;
		}
	}
	
	@Override
	public void destroy() {
		this.finishSound.close();
		super.destroy();
	}
	
}
