package platformerTest.weapons.shopWeaponsT3;

import javax.imageio.ImageIO;

import platformerTest.assets.effects.Effect;
import platformerTest.assets.effects.EffectFire;
import platformerTest.assets.effects.EffectPoison;
import platformerTest.game.LivingObject;
import platformerTest.weapons.Weapon;

public class BladeOfImmolation extends Weapon {
	
	public int attackDamage = 1;
	public int attackRange = 3;
	public int attackCooldown = 10;
	
	public BladeOfImmolation() {
		try {
			this.coinCost = 7500;
			this.inShop = 1;
			
			this.size = 30;
			this.name = "Blade of Immolation";
			this.attackSound = "/sounds/attack/sword/attack.wav";
			this.tier = 3;
			
			this.stats = new String[]{"Attack Damage +20%", "Attack Range +15%", "Attack Speed -25%",
					"On hit: Set both on fire", "(4 dmgticks, 1 dmg per)", 
					"Damage mult stacks 5x"};
			this.statMap = new int[] {1, 1, -1, 2, 2, 2};
			
			this.lore = "This demonic sword is the result of brittle volcanic obsidian forged under tremendous pressure, "
					+ "molding into a weapon which lacks none of the force experienced in forging in igniting a fiery "
					+ "conflagration.";
			
			this.image = ImageIO.read(this.getClass().getResource("/weapons/shopWeaponsT3/BladeOfImmolation.png"));

		} catch (Exception e) {}
	}
	
	@Override
	public void onAttackStart(LivingObject wielder, LivingObject victim) {
		Effect fire = victim.hasEffect("Fire");
		if (fire != null) {
			int strength = (fire.strength >= 5) ? 5 : fire.strength + 1;
			victim.applyEffect(new EffectFire(4*45, strength, wielder));
			
		} else victim.applyEffect(new EffectFire(4*45, 1, wielder));
	}
	
	@Override
	public void init(LivingObject l) {
		this.attackSound = "/sounds/attack/sword/attack.wav";
		l.maxAttackCooldown += this.attackCooldown;
		l.attackDamage += this.attackDamage;
		l.attackRange += this.attackRange;
	}
}