package platformerTest.assets.creature.creatures;

import java.awt.Color;

import platformerTest.assets.creature.ai.AttackAi;
import platformerTest.assets.creature.ai.attack.NormalMovementAttackAi;
import platformerTest.assets.creature.ai.horizontal.HorizontalFollowAi;
import platformerTest.assets.creature.ai.vertical.VerticalFollowAi;
import platformerTest.game.Creature;
import platformerTest.menu.GamePanel;

public class CreatureGoblin extends Creature {

	public static final Color COLOR_GOBLIN = new Color(118,255,122);
	
	public CreatureGoblin(double initX, double initY, double size) {
		this(initX, initY, size, 0, Double.MAX_VALUE);
	}
	
	public CreatureGoblin(double initX, double initY, double size, double minRange, double maxRange) {
		super(initX, initY, size, COLOR_GOBLIN, Color.GRAY, 1, 20, 0.15, 12, 3, 7, 30, 1);
		this.friendlyFire = false;
		
		this.aiList.add(new HorizontalFollowAi(minRange, maxRange, GamePanel.player));
		this.aiList.add(new VerticalFollowAi(0, 200, GamePanel.player));
		this.aiList.add(new NormalMovementAttackAi(this.attackRange/2, GamePanel.player));
	}
	
	public CreatureGoblin(double initX, double initY, double size, double minRangeX, double maxRangeX, double minRangeY, double maxRangeY) {
		super(initX, initY, size, COLOR_GOBLIN, Color.GRAY, 1, 20, 0.15, 12, 3, 7, 30, 1);
		this.friendlyFire = false;
		
		this.aiList.add(new HorizontalFollowAi(minRangeX, maxRangeX, minRangeY, maxRangeY, GamePanel.player));
		this.aiList.add(new VerticalFollowAi(75, 200, 6, 100, minRangeX, maxRangeX, GamePanel.player));
		this.aiList.add(new NormalMovementAttackAi(this.attackRange/2, GamePanel.player));
	}

}