package platformerTest.weapons.starterWeapons;

import java.io.BufferedInputStream;

import javax.imageio.ImageIO;

import platformerTest.game.LivingObject;
import platformerTest.weapons.Weapon;

public class SharpSword extends Weapon {
	
	public int attackDamage = 3;
	public int attackCooldown = 10;
	public int attackRange = 4;
	
	public SharpSword() {
		try {
			this.coinCost = 1000;
			this.inShop = true;
			
			this.size = 30;
			this.name = "Sharp Sword";
			this.attackSound = new BufferedInputStream(this.getClass().getResourceAsStream("/sounds/attack/sword/attack.wav"));
			this.tier = 1;
			
			this.stats = new String[]{"Attack Damage +60%", "Attack Range +20%", "Attack Speed -25%"}; //1.28x dmg
			this.statMap = new int[] {1, 1, -1};
			
			this.lore = "Reliable melee weapon for any adventure, but a bit slow to swing for the untrained user.";
			
			this.image = ImageIO.read(this.getClass().getResource("/weapons/starterWeapons/SharpSword.png"));

		} catch (Exception e) {}
	}
	
	@Override
	public void init(LivingObject l) {
		this.attackSound = new BufferedInputStream(this.getClass().getResourceAsStream("/sounds/attack/sword/attack.wav"));
		l.attackDamage += this.attackDamage;
		l.maxAttackCooldown += this.attackCooldown;
		l.attackRange += this.attackRange;
	}
}