package skycubedPlatformer.menu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import skycubedPlatformer.Main;
import skycubedPlatformer.appdata.DataManager;
import skycubedPlatformer.assets.effects.Effect;
import skycubedPlatformer.assets.triggers.Powerup;
import skycubedPlatformer.game.GameObject;
import skycubedPlatformer.game.LivingObject;
import skycubedPlatformer.game.ObjType;
import skycubedPlatformer.game.Player;
import skycubedPlatformer.levels.Level;

@SuppressWarnings("serial")
public class GamePanel extends JPanel {
	
	public static Player player;
	public static Level level;
	
	public static double airDrag;
	public static double gravity;
	public static List<GameObject> objects;
	public static List<GameObject> projectiles;
	public static List<GameObject> particles;
	public static List<GameObject> deletedObjects;
	public static List<GameObject> addedObjects;
	
	public static double camera_x;
	public static double camera_y;
	public static double camera_size;
	public static int target_camera_size;
	
	public static boolean isPaused;
	public static int levelWon;
	
	public static Timer timer;
	public static long coins;
	public static long targetCoins;
	public static int timeSinceRestart = 0;
	
	public GamePanel(Level level) {
		this.setBackground(Color.BLACK);
		this.setSize(Main.SIZE, Main.SIZE);
		this.setVisible(true);
		this.setFocusable(true);
		this.addKeyListener(new Keyboard());
		this.addMouseListener(new PauseMenuMouse());
		
		coins = 0;
		restartLevel(level);
		timeSinceRestart = 180;
		loadImages();
		
		timer = new Timer(1000/90, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onTick();
				repaint();
		}});
		timer.start();
		
		levelWon = 0;
		isPaused = false;
		
	}
	
	public static void restartLevel(Level newLevel) {
		timeSinceRestart = 0;
		
		//wipe old data if old data exists
		destroyLevel();
		
		targetCoins = 0;
		
		displayText = null;
		textDuration = 0;
		
		level = newLevel;
		
		level.drawBackground();
		
		player = new Player(level.spawnX, level.spawnY, 40);
		objects.add(player);
		
		camera_x = (int) player.x;
		camera_y = (int) player.y;
		camera_size = Main.SIZE;
		target_camera_size = Main.SIZE;
	
		airDrag = level.airDrag;
		gravity = level.gravity;
		
		level.drawPlatforms();
		level.drawForeground();
		
		level.onStart();
		
		createFlash(Color.white,100);
		
		System.gc();

	}
	
	public void onTick() {
		if (timeSinceRestart < 1000) timeSinceRestart++;
		
		if (!isPaused) {
			level.onTick();
			player.move();
			moveCamera();
		}
		
		for (GameObject obj : objects) {
			if (!obj.equals(player) && !isPaused) {
				obj.move();
			}
		}
		
		for (GameObject obj : deletedObjects) {
			objects.remove(obj);
			obj.destroy();
		}
		
		objects.addAll(projectiles);
		objects.addAll(particles);
		objects.addAll(addedObjects);
		
		addedObjects.clear();
		projectiles.clear();
		particles.clear();
		deletedObjects.clear();
		
		//check if level won
		if (!(levelWon == 0)) levelWon++;
		if (levelWon>360) {
			if (!DataManager.saveData.completedLevels.containsKey(level.getClass().getSimpleName())) {
				DataManager.saveData.completedLevels.put(level.getClass().getSimpleName(), 1);
			} else {
				DataManager.saveData.completedLevels.replace(level.getClass().getSimpleName(),
				DataManager.saveData.completedLevels.get(level.getClass().getSimpleName())+1);
			}
			destroyLevel();
			timer.stop();
			destroy();
			Main.jframe.openLevelSelect(level);
			
		}
		
		//update flashes
		ArrayList<Color> removedFlashes = new ArrayList<Color>();
		flashes.forEach((Color c, Integer i) -> {
			if (i<2) removedFlashes.add(c);
			else flashes.replace(c, i, i-1);
		});
		for (Color c : removedFlashes) flashes.remove(c);
		
		if (!this.isFocusOwner()) {
			if (levelWon == 0 && player.isAlive && !isPaused) isPaused = true;
		}
	}
	
	/**Order: Level tick -> Player move -> Camera move -> Background -> Level Paint -> draw attacks -> Draw ambience
	-> Create flash effects -> Fade out if falling into void -> draw HUD -> display text -> win fading
	**/
	public void paint(Graphics g) {
		super.paint(g);
		level.fill((Graphics2D) g);
		
		for (GameObject obj : objects) {
			if (obj.hasCollided(MainFrameObj) || obj.type.equals(ObjType.Creature) || obj.type.equals(ObjType.Player)) {
				obj.draw(g, player, camera_x, camera_y, camera_size);
			}
		}
		
		//draw attacks
		if (!isPaused) drawAttacks(g);
		if (!isPaused) level.drawAmbience(g);
		
		//draw flashes
		drawFlashes(g);
		drawHUD(g);
		
		//display text
		if (textDuration != 0) {
			int alpha = 255;
			if (textDuration <= 120) alpha = 255*textDuration/120;
			Graphics2D g2d = (Graphics2D) g;
			GradientPaint gp2 = new GradientPaint(0, Main.SIZE-100, new Color(255,255,255,0), 0, Main.SIZE-75, new Color(255,255,255,alpha), false);
			g2d.setPaint(gp2);
			g2d.fillRect(-50, Main.SIZE-125, Main.SIZE+50, Main.SIZE);
			Font font = new Font(Font.MONOSPACED, Font.BOLD, 25);
			g.setFont(font);
			g.setColor(new Color(100,100,100,alpha));
			int lvlSelectStringWidth = g.getFontMetrics(font).stringWidth(displayText);
			g.drawString(displayText, Main.SIZE/2 - lvlSelectStringWidth/2, Main.SIZE-50);
			
			textDuration--;
		}
		
		//pause menu
		if (isPaused) {
			drawPauseMenu(g);
		}
		
		//draw win flash
		if (levelWon < 240 && levelWon > 0) {
			g.setColor(new Color(255,255,255,255*(240-levelWon)/240));
			g.fillRect(-50, -50, Main.SIZE+50, Main.SIZE + 50);
			}
		if (levelWon > 240) {
			g.setColor(new Color(0,0,0,255*(levelWon-241)/120));
			g.fillRect(-50, -50, Main.SIZE+50, Main.SIZE + 50);
		}
		if (levelWon>360) {
			g.setColor(new Color(0,0,0));
			g.fillRect(-50, -50, Main.SIZE+50, Main.SIZE + 50);
		}
		
	}
	
	static int buttonSizeX=400;
	static int buttonSizeY=100;
	
	public void drawPauseMenu(Graphics g) {
		g.setColor(new Color(0,0,0,200));
		g.fillRect(-50, -50, Main.SIZE+50, Main.SIZE + 50);
		
		Font font = new Font(Font.MONOSPACED, Font.BOLD, 50);
		g.setFont(font);
		g.setColor(Color.WHITE);
		int lvlSelectStringWidth = g.getFontMetrics(font).stringWidth("Game Paused");
		g.drawString("Game Paused", Main.SIZE/2 - lvlSelectStringWidth/2, 75);
		
		Graphics2D g2d = (Graphics2D) g;
		font = new Font(Font.MONOSPACED, Font.BOLD, 40);
		g.setFont(font);
		
		g2d.setColor(Color.GRAY);
		g2d.fillRoundRect(Main.SIZE/2-buttonSizeX/2, Main.SIZE/4-buttonSizeY/2, buttonSizeX, buttonSizeY, 5, 5);
		
		g2d.setColor(Color.GRAY);
		g2d.fillRoundRect(Main.SIZE/2-buttonSizeX/2, Main.SIZE/2-buttonSizeY/2, buttonSizeX, buttonSizeY, 5, 5);
		
		g2d.setColor(Color.GRAY);
		g2d.fillRoundRect(Main.SIZE/2-buttonSizeX/2, Main.SIZE*3/4-buttonSizeY/2, buttonSizeX, buttonSizeY, 5, 5);
		
		Point mousePosition = this.getMousePosition();
		if (mousePosition != null) {
			int mouseX = mousePosition.x;
			int mouseY = mousePosition.y;
			
			if (Math.abs(mouseX - Main.SIZE/2) < buttonSizeX/2 && Math.abs(mouseY - Main.SIZE/4) < buttonSizeY/2) {
				g2d.setColor(new Color(255, 255, 255, 100));
				g2d.fillRect(Main.SIZE/2-buttonSizeX/2, Main.SIZE/4-buttonSizeY/2, buttonSizeX, buttonSizeY);
			}
			
			if (Math.abs(mouseX - Main.SIZE/2) < buttonSizeX/2 && Math.abs(mouseY - Main.SIZE/2) < buttonSizeY/2) {
				g2d.setColor(new Color(255, 255, 255, 100));
				g2d.fillRect(Main.SIZE/2-buttonSizeX/2, Main.SIZE/2-buttonSizeY/2, buttonSizeX, buttonSizeY);
			}
			
			if (Math.abs(mouseX - Main.SIZE/2) < buttonSizeX/2 && Math.abs(mouseY - Main.SIZE*3/4) < buttonSizeY/2) {
				g2d.setColor(new Color(255, 255, 255, 100));
				g2d.fillRect(Main.SIZE/2-buttonSizeX/2, Main.SIZE*3/4-buttonSizeY/2, buttonSizeX, buttonSizeY);
			}
			
			
		}
		
		g2d.setStroke(new BasicStroke(5));
		g2d.setColor(Color.WHITE);
		g.drawRoundRect(Main.SIZE/2-buttonSizeX/2, Main.SIZE/4-buttonSizeY/2, buttonSizeX, buttonSizeY, 5, 5);
		
		int StringWidth = g.getFontMetrics(font).stringWidth("Unpause Game");
		int StringHeight = g.getFontMetrics(font).getHeight();
		g2d.drawString("Unpause Game", Main.SIZE/2-StringWidth/2, Main.SIZE/4+10);
		
		g2d.setStroke(new BasicStroke(5));
		g2d.setColor(Color.WHITE);
		g.drawRoundRect(Main.SIZE/2-buttonSizeX/2, Main.SIZE/2-buttonSizeY/2, buttonSizeX, buttonSizeY, 5, 5);
		
		StringWidth = g.getFontMetrics(font).stringWidth("Restart Level");
		g2d.drawString("Restart Level", Main.SIZE/2-StringWidth/2, Main.SIZE/2+10);
		
		g2d.setStroke(new BasicStroke(5));
		g2d.setColor(Color.WHITE);
		g.drawRoundRect(Main.SIZE/2-buttonSizeX/2, Main.SIZE*3/4-buttonSizeY/2, buttonSizeX, buttonSizeY, 5, 5);
		
		StringWidth = g.getFontMetrics(font).stringWidth("Exit to Menu");
		g2d.drawString("Exit to Menu", Main.SIZE/2-StringWidth/2, Main.SIZE*3/4+10);
		
		
		
	}
	
	public void moveCamera() {
		level.moveCamera();
		
		MainFrameObj.x = camera_x;
		MainFrameObj.y = camera_y;
		MainFrameObj.size_x = camera_size + 50;
		MainFrameObj.size_y = camera_size + 50;
		
		//smooth movement
		if (Math.abs(camera_size - target_camera_size) < 10) camera_size = target_camera_size;
		if (camera_size > target_camera_size + 100) camera_size = camera_size - 10;
		else if (camera_size > target_camera_size) camera_size = camera_size - 5;
		else if (camera_size < target_camera_size - 100) camera_size = camera_size + 10;
		else if (camera_size < target_camera_size) camera_size = camera_size + 5;
		
	}

	public static String displayText = null;
	public static int textDuration = 0;
	
	public static void displayText(String newText, int newDuration) {
		displayText = newText;
		textDuration = newDuration;
	}

	int[] powerupX = new int[] {5, 5, 75, 75, 145, 145, 215, 215, 285, 285, 355, 355, 425, 425, 495, 495, 565, 565, 635, 635};
	int[] powerupY = new int[] {4, 29, 4, 29, 4, 29, 4, 29, 4, 29, 4, 29, 4, 29, 4, 29, 4, 29, 4, 29};
	
	public void drawHUD(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		//top
		GradientPaint gp1 = new GradientPaint(0, 50, Color.white, 0, 80, new Color(255,255,255,0), false);
		g2d.setPaint(gp1);
		g2d.fillRect(-50, 0, Main.SIZE+50, 80);
		//render healthbar
		if (player.health > player.maxHealth) player.health = player.maxHealth;
		if (player.health < 0) player.health = 0;
		
		g2d.drawImage(healthImage, Main.SIZE*3/4+25, 10, 30, 30, null);
		g2d.setColor(Color.black);
		g2d.fillRoundRect(Main.SIZE*3/4 + 70, 10, 100, 30, 5, 5);
		g2d.setColor(Color.red);
		g2d.fillRoundRect(Main.SIZE*3/4 + 70, 10, player.health * (player.maxHealth/100), 30, 5, 5);
	
		if (player.overheal != 0) {
			int overHeal = player.overheal;
			if (player.overheal > player.maxHealth) overHeal = player.maxHealth;
			g2d.setColor(GameObject.COLOR_GOLD);
			g2d.fillRoundRect(Main.SIZE*3/4 + 70, 10, overHeal * (player.maxHealth/100), 30, 5, 5);
		}
		
		if (player.overheal > 100) {
			int gigaHeal = player.overheal-100;
			if (gigaHeal > player.maxHealth) gigaHeal = player.maxHealth;
			g2d.setColor(GameObject.COLOR_DIAMOND);
			g2d.fillRoundRect(Main.SIZE*3/4 + 70, 10, gigaHeal * (player.maxHealth/100), 30, 5, 5);
		}
			
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(3));
		g2d.drawRoundRect(Main.SIZE*3/4 + 70, 10, 100, 30, 5, 5);
		
		//COINS COINS COINS COINS COINS COINS COINS COINS COINS COINS
		
		if (coins != targetCoins) {
			//smooth movement
			if (coins > targetCoins + 2500) coins -= 189;
			if (coins > targetCoins + 500) coins -= 63;
			if (coins > targetCoins + 100) coins -= 21;
			else if (coins > targetCoins + 20) coins -= 7;
			else if (coins > targetCoins) coins--;
			else if (coins < targetCoins - 2500) coins += 189;
			else if (coins < targetCoins - 500) coins += 63;
			else if (coins < targetCoins - 100) coins += 21;
			else if (coins < targetCoins - 20) coins += 7;
			else if (coins < targetCoins) coins++;
		}
		
		int diff = Main.SIZE*3/5;
		
		g2d.setColor(GameObject.COLOR_GOLD);
		g2d.setColor(new Color(200,200,200));
		g2d.fillRoundRect(diff+50,10,80,30,5,5);
		g2d.drawImage(goldCoinImage, diff+5, 10, 30, 30, null);
		g2d.setColor(Color.BLACK);
		g2d.drawRoundRect(diff+50,10,80,30,5,5);
		
		Font font = new Font(Font.MONOSPACED, Font.BOLD, 20);
		g2d.setFont(font);
		g2d.setColor(Color.BLACK);
		int coinTextWidth = g2d.getFontMetrics(font).stringWidth(coins+"");
		int coinTextHeight = g2d.getFontMetrics(font).getHeight();
		g2d.drawString(coins+"", diff+90-(coinTextWidth/2), 31);
		
		//POWERUPS POWERUPS POWERUPS POWERUPS POWERUPS POWERUPS POWERUPS POWERUPS 
		g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
		
		int i = 0;
		BufferedImage image;
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		df.setMinimumIntegerDigits(1);
			
		//density
		if (player.density != 1) {
			g2d.setColor(Powerup.COLOR_POWERUP_DENSITY);
			g2d.fillOval(powerupX[i], powerupY[i], 22, 22);
			g2d.drawImage(densityImage, powerupX[i], powerupY[i], 22, 22, null);
			g2d.setColor((player.density > 1) ? Color.darkGray : Color.gray);
			g2d.drawString("x"+df.format(player.density), powerupX[i]+23, powerupY[i]+15);
			i++;}
		//attack speed
		if (player.maxAttackCooldown != 40) {
			g2d.setColor(Powerup.COLOR_POWERUP_ATTACKSPEED);
			g2d.fillOval(powerupX[i], powerupY[i], 22, 22);
			g2d.drawImage(attackSpeedImage, powerupX[i], powerupY[i], 22, 22, null);
			g2d.setColor((player.maxAttackCooldown < 40) ? Color.green : Color.red);
			g2d.drawString("x"+df.format(40.0/player.maxAttackCooldown), powerupX[i]+23, powerupY[i]+15);
			i++;}
		//strength
		if (player.attackDamage != 5) {
			g2d.setColor(Powerup.COLOR_POWERUP_STRENGTH);
			g2d.fillOval(powerupX[i], powerupY[i], 22, 22);
			g2d.drawImage(strengthImage, powerupX[i], powerupY[i], 22, 22, null);
			g2d.setColor((player.attackDamage>5) ? Color.green : Color.red);
			g2d.drawString("x"+df.format(player.attackDamage/5.0), powerupX[i]+23, powerupY[i]+15);
			i++;}
		//fire resistance
		if (player.fireResistant) {
			g2d.setColor(Powerup.COLOR_POWERUP_FIRERESISTANCE);
			g2d.fillOval(powerupX[i], powerupY[i], 22, 22);
			g2d.drawImage(fireResistanceImage, powerupX[i], powerupY[i], 22, 22, null);
			g2d.setColor(new Color(255,100,0));
			g2d.drawString("✔", powerupX[i]+23, powerupY[i]+15);
			i++;}
		//overheal
		if (player.overheal > 0) {
			g2d.setColor(Powerup.COLOR_POWERUP_OVERHEAL);
			g2d.fillOval(powerupX[i], powerupY[i], 22, 22);
			g2d.drawImage(overhealImage, powerupX[i], powerupY[i], 22, 22, null);
			g2d.setColor((player.overheal>100) ? GameObject.COLOR_DIAMOND : new Color(230,230,0));
			g2d.drawString(""+player.overheal, powerupX[i]+23, powerupY[i]+15);
			i++;}
		//jump boost
		if (player.jumpStrength != 16) {
			g2d.setColor(Powerup.COLOR_POWERUP_JUMPBOOST);
			g2d.fillOval(powerupX[i], powerupY[i], 22, 22);
			g2d.drawImage(jumpBoostImage, powerupX[i], powerupY[i], 22, 22, null);
			g2d.setColor((player.jumpStrength>16) ? Color.green : Color.red);
			g2d.drawString(((player.jumpStrength>16)?"+":"-")+Math.abs(player.jumpStrength-16), powerupX[i]+23, powerupY[i]+15);
			i++;}
		//camera size
		if (camera_size != Main.SIZE) {
			g2d.setColor(Powerup.COLOR_POWERUP_CAMERASIZE);
			g2d.fillOval(powerupX[i], powerupY[i], 22, 22);
			g2d.drawImage(cameraSizeImage, powerupX[i], powerupY[i], 22, 22, null);
			g2d.setColor((camera_size>Main.SIZE) ? Color.green : Color.red);
			g2d.drawString("x"+df.format(camera_size/Main.SIZE), powerupX[i]+23, powerupY[i]+15);
			i++;}
		//swiftness
		if (player.movementSpeed != 0.25) {
			g2d.setColor(Powerup.COLOR_POWERUP_SWIFTNESS);
			g2d.fillOval(powerupX[i], powerupY[i], 22, 22);
			g2d.drawImage(swiftnessImage, powerupX[i], powerupY[i], 22, 22, null);
			g2d.setColor((player.movementSpeed>0.25) ? Color.green : Color.red);
			g2d.drawString("x"+df.format(player.movementSpeed/0.25), powerupX[i]+23, powerupY[i]+15);
			i++;}
		//punch
		if (player.attackKnockback != 2) {
			g2d.setColor(Powerup.COLOR_POWERUP_PUNCH);
			g2d.fillOval(powerupX[i], powerupY[i], 22, 22);
			g2d.drawImage(punchImage, powerupX[i], powerupY[i], 22, 22, null);
			g2d.setColor((player.attackKnockback>2) ? Color.green : Color.red);
			g2d.drawString("x"+df.format(player.attackKnockback/2), powerupX[i]+23, powerupY[i]+15);
			i++;}
		//marksman
		if (player.attackRange != 20) {
			g2d.setColor(Powerup.COLOR_POWERUP_RANGE);
			g2d.fillOval(powerupX[i], powerupY[i], 22, 22);
			g2d.drawImage(rangeImage, powerupX[i], powerupY[i], 22, 22, null);
			g2d.setColor((player.attackRange>5) ? Color.green : Color.red);
			g2d.drawString("x"+df.format(player.attackRange/20.0), powerupX[i]+23, powerupY[i]+15);
			i++;}
		//effects
		for (int j=0; j<player.effects.size(); j++) {
			Effect e = player.effects.get(j);
			g2d.drawImage(e.image, powerupX[j+i], powerupY[j+i], 22, 22, null);
			g2d.setColor(e.color);
			g2d.drawString(e.strength + "x" + (e.lifetime / e.delay), powerupX[j+i]+23, powerupY[j+i]+15);
		}

	}
	
	public static HashMap<Color,Integer> flashes = new HashMap<Color,Integer>();
	
	public static void createFlash(Color color, int duration) {
		if (flashes.containsKey(color) && flashes.get(color) < duration) flashes.replace(color, duration);
		else flashes.put(color, duration);
	}
	
	public void drawFlashes(Graphics g) {
		//flash effects
		flashes.forEach((Color c, Integer i) -> {
			int alpha = (i > 255) ? 255 : i;
			Color newColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
			g.setColor(newColor);
			g.fillRect(-50, -50, Main.SIZE+50, Main.SIZE + 50);
				
		});
				
		//fade out upon reaching bottom of level
		if (player.y < (level.bottomLimit + 1000) && levelWon == 0) {
			int alpha = (int)(player.y - level.bottomLimit)* 255/1000;
			if (alpha < 0) alpha = 0;
			g.setColor(new Color(0,0,0,255-alpha));
			g.fillRect(-50, -50, Main.SIZE+50, Main.SIZE + 50);
		} else if (player.y > (level.topLimit - 1000) && levelWon == 0) {
			int alpha = -(int)(player.y - level.topLimit)* 255/1000;
			if (alpha < 0) alpha = 0;
			g.setColor(new Color(0,0,0,255-alpha));
			g.fillRect(-50, -50, Main.SIZE+50, Main.SIZE + 50);
		}
	}
	
	public void drawAttacks(Graphics g) {  //ONLY DRAWS MELEE ATTACKS and Render Weapons
		Graphics2D g2d = (Graphics2D) g;
		for (GameObject obj : objects) {
			if ((obj.type.equals(ObjType.Creature) && obj.hasCollided(MainFrameObj)) | obj.type.equals(ObjType.Player)) {
					LivingObject c = (LivingObject) obj;
					if (!(c.meleeCooldown == 0) && !(c.maxAttackCooldown - c.meleeCooldown > 20)) {
						int alpha = (20-(c.maxAttackCooldown - c.meleeCooldown))*255/20;
						
						int lastAttackX = (int) (c.x + c.lastAttackRange * Math.cos(c.lastAttackAngle * Math.PI/180));
						int lastAttackY = (int) (c.y + c.lastAttackRange * Math.sin(c.lastAttackAngle * Math.PI/180));
						
						int drawX = (int) ((lastAttackX - (c.size_x)/2 - (camera_x - camera_size/2)) * (Main.SIZE/camera_size));
						int drawY = (int) ((camera_size - (lastAttackY + (c.size_y)/2) + (camera_y - camera_size/2)) * (Main.SIZE/camera_size));
						int sizeX = (int) ((c.size_x) * Main.SIZE/camera_size);
						int sizeY = (int) ((c.size_y) * Main.SIZE/camera_size);
						
						Arc2D arc = new Arc2D.Double(drawX, drawY, sizeX, sizeY, c.lastAttackAngle-45, 90, Arc2D.OPEN);
						g2d.setColor(new Color(c.color.getRed(),c.color.getGreen(),c.color.getBlue(),alpha));
						g2d.setStroke(new BasicStroke((float) (5*Main.SIZE/camera_size)));
						g2d.draw(arc);
					}
					
					//DRAW WEAPON
					if (c.weapon != null && c.isAlive) {
						int size = (int) (c.weapon.size*(Main.SIZE/camera_size)*(c.size_x/40));
						Graphics2D g2 = (Graphics2D) g2d.create();
						BufferedImage image = c.weapon.image;
						int angle = (c.maxAttackCooldown - c.attackCooldown < c.maxAttackCooldown/8) ? 
								90 * (c.maxAttackCooldown - c.attackCooldown)/(c.maxAttackCooldown/8) :
									(c.maxAttackCooldown - c.attackCooldown)<(c.maxAttackCooldown/2) ?
										90 : 90*(c.attackCooldown)/(c.maxAttackCooldown/2);
						
						if (c.attackCooldown != 0) {
							if (c.lastAttackAngle == 90) angle -= 90;
							else if (c.lastAttackAngle == 45 || c.lastAttackAngle == 135) angle -= 45;
							else if (c.lastAttackAngle == -45 || c.lastAttackAngle == -135) angle += 45;
							else if (c.lastAttackAngle == -90) angle += 90;
						} else angle = 0;
						
						double rads = Math.toRadians(angle);
						
						int drawX, drawY;
						if (c.lastDirection == 1) {
							drawX = (int) ( (c.x + (c.size_x)/2 - (camera_x - camera_size/2) - 5) * (Main.SIZE/camera_size)); //upwards
							drawY = (int) ( (camera_size - (c.y + (c.size_y/2)) + (camera_y - camera_size/2)) * (Main.SIZE/camera_size)) + ((int) (0.75 * c.size_y * (Main.SIZE/camera_size) - size));
						} else {
							drawX = (int) ( (c.x - (c.size_x)/2 - (camera_x - camera_size/2) + 5) * (Main.SIZE/camera_size)); //upwards
							drawY = (int) ( (camera_size - (c.y + (c.size_y/2)) + (camera_y - camera_size/2)) * (Main.SIZE/camera_size)) + ((int) (0.75 * c.size_y * (Main.SIZE/camera_size) - size));
						}
						
						g2.rotate(rads * (c.lastDirection), drawX, drawY+size);
						g2.drawImage(image, drawX, drawY, (c.lastDirection) * size, size, null);
					}
					
		}}

		
	}
	
	public class Keyboard extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE && levelWon == 0 && player.isAlive) isPaused = isPaused? false : true;
			if (isPaused) return;
			if (e.getKeyCode() == KeyEvent.VK_SPACE) player.isAttacking = true; //SPACE
			if (e.getKeyCode() == KeyEvent.VK_W) player.movingUp = true; //W
			if (e.getKeyCode() == KeyEvent.VK_A) {
				player.movingLeft = true; //A
				player.lastDirection = -1;
			}
			if (e.getKeyCode() == KeyEvent.VK_S) player.movingDown = true; //S
			if (e.getKeyCode() == KeyEvent.VK_D) {
				player.movingRight = true; //D
				player.lastDirection = 1;
			}
			if (e.getKeyCode() == KeyEvent.VK_R && levelWon == 0 && timeSinceRestart > 90) GamePanel.restartLevel(level);
			
			if (e.getKeyCode() == KeyEvent.VK_X && levelWon == 0) System.out.println(player.x + ", " + player.y);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) player.isAttacking = false;
			if (e.getKeyCode() == KeyEvent.VK_W) player.movingUp = false; //W
			if (e.getKeyCode() == KeyEvent.VK_A) player.movingLeft = false; //A
			if (e.getKeyCode() == KeyEvent.VK_S) player.movingDown = false; //S
			if (e.getKeyCode() == KeyEvent.VK_D) player.movingRight = false; //D
		}
	}
	
	public class PauseMenuMouse extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (!isPaused) return;
			int mouseX = e.getX();
			int mouseY = e.getY();
			
			if (Math.abs(mouseX - Main.SIZE/2) < buttonSizeX/2 && Math.abs(mouseY - Main.SIZE/4) < buttonSizeY/2) {
				isPaused = false;
			}
			
			if (Math.abs(mouseX - Main.SIZE/2) < buttonSizeX/2 && Math.abs(mouseY - Main.SIZE/2) < buttonSizeY/2) {
				isPaused = false;
				GamePanel.restartLevel(level);
			}
			
			if (Math.abs(mouseX - Main.SIZE/2) < buttonSizeX/2 && Math.abs(mouseY - Main.SIZE*3/4) < buttonSizeY/2) {
				timer.stop();
				destroyLevel();
				destroy();
				Main.jframe.openLevelSelect(level);
			}
			
		}
	}
	
	public static final GameObject MainFrameObj = new GameObject(0, 0, Main.SIZE+50, Main.SIZE+50, null);
	
	public static BufferedImage healthImage, copperCoinImage, silverCoinImage, goldCoinImage, gemImage,
	densityImage, attackSpeedImage, strengthImage, fireResistanceImage, overhealImage,
	jumpBoostImage, cameraSizeImage, swiftnessImage, punchImage, rangeImage;
	public void loadImages() {
		try {
			healthImage = ImageIO.read(this.getClass().getResource("/gui/health.png"));
			copperCoinImage = ImageIO.read(this.getClass().getResource("/gui/coppercoin.png"));
			silverCoinImage = ImageIO.read(this.getClass().getResource("/gui/silvercoin.png"));
			goldCoinImage = ImageIO.read(this.getClass().getResource("/gui/goldcoin.png"));
			gemImage = ImageIO.read(this.getClass().getResource("/gui/gem.png"));
			
			densityImage = ImageIO.read(this.getClass().getResource("/powerups/density.png"));
			attackSpeedImage = ImageIO.read(this.getClass().getResource("/powerups/attackspeed.png"));
			strengthImage = ImageIO.read(this.getClass().getResource("/powerups/strength.png"));
			fireResistanceImage = ImageIO.read(this.getClass().getResource("/powerups/fireresistance.png"));
			overhealImage = ImageIO.read(this.getClass().getResource("/powerups/overheal.png"));
			jumpBoostImage = ImageIO.read(this.getClass().getResource("/powerups/jumpboost.png"));
			cameraSizeImage = ImageIO.read(this.getClass().getResource("/powerups/camerasize.png"));
			swiftnessImage = ImageIO.read(this.getClass().getResource("/powerups/swiftness.png"));
			punchImage = ImageIO.read(this.getClass().getResource("/powerups/punch.png"));
			rangeImage = ImageIO.read(this.getClass().getResource("/powerups/range.png"));

		} catch (Exception e) {e.printStackTrace();}
	}
	
	public static void destroyLevel() {
		//wipe all shit in all objects
		if (objects != null) {
			for (GameObject obj : objects) {
				obj.destroy();
			}
		}
		
		//destroy the level
		if (level != null) level.destroy();
		level = null;
		player = null;
		
		targetCoins = 0;
		displayText = null;
		textDuration = 0;
		
		//now wipe the arrays
		objects = new ArrayList<GameObject>();
		deletedObjects = new ArrayList<GameObject>();
		flashes = new HashMap<Color,Integer>();
		projectiles = new ArrayList<GameObject>();
		particles = new ArrayList<GameObject>();
		addedObjects = new ArrayList<GameObject>();
	}
	
	public void destroy() {
		
	}
}