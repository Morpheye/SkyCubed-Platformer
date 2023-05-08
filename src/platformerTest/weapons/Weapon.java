package platformerTest.weapons;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import platformerTest.game.LivingObject;
import platformerTest.weapons.shopWeaponsT2.*;
import platformerTest.weapons.shopWeaponsT3.*;
import platformerTest.weapons.shopWeaponsT4.*;
import platformerTest.weapons.starterWeapons.*;

public class Weapon {

	public BufferedImage image;
	public int size = 0;
	public int tier = 0;
	public String name;
	public String[] stats;
	public int[] statMap;
	public String lore;
	public int coinCost = 0;
	public int gemCost = 0;
	public boolean isRanged = false;
	public boolean inShop = false;
	public InputStream attackSound;
	public InputStream hitSound;
	
	public Weapon() {
		
	}

	public void init(LivingObject creature) {}
	
	public static HashMap<String,Weapon> weapons = new HashMap<String,Weapon>();
	public static ArrayList<Weapon> weaponList = new ArrayList<Weapon>();
	public static ArrayList<String> weaponNames = new ArrayList<String>();
	public static Weapon getWeapon(String name) {
		if (weapons.containsKey(name)) return weapons.get(name);
		else return null;
	}
	private static void addWeapon(Weapon weapon) {
		weapons.put(weapon.name, weapon);
		weaponList.add(weapon);
		weaponNames.add(weapon.name);
	}
	public static void weaponListInit() {
		//beginner weapons (tier 1: Bronze)
		addWeapon(new SharpSword());
		addWeapon(new SharpAxe());
		addWeapon(new PointedSpear());
		addWeapon(new WoodenClub());
		addWeapon(new SwiftDagger());
		//moderate weapons (tier 2: Silver)
		addWeapon(new DuelingFoil());
		addWeapon(new HeavyMace());
		addWeapon(new AquaforgedTrident());
		addWeapon(new AerialStaff());
		addWeapon(new GoldenKnife());
		//high-class weapons (tier 3: Gold)
		addWeapon(new PoisonEdgeKatana());
		addWeapon(new ExecutionerAxe());
		addWeapon(new BejeweledMoonstaff());
		//exotic weapons (tier 4: diamond)
		addWeapon(new GildedChimeraBlade());
		//ultra-exotic weapons (tier 5: crimsonade)
	}
	
	/**
	 * Triggers when wielder hits something. Use this for modifying damage
	 */
	public void onAttackStart(LivingObject wielder, LivingObject victim) {}
	
	/**
	 * Triggers after damage dealt. Use this to undo effects of onAttackStart()
	 */
	public void onAttackEnd(LivingObject wielder, LivingObject victim) {}
	
	/**
	 * Triggers when victim takes damage, if it dies.
	 */
	public void onKill(LivingObject wielder, LivingObject victim) {}
	
	/**
	 * 
	 */
	public void onUserHit(LivingObject wielder, LivingObject attacker) {}
	
	/**
	 * What the weapon does when used if it is ranged
	 */
	public void rangedAttack(LivingObject wielder, int angle) {}
	
	
}