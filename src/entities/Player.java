package entities;

import static utilz.Constants.PlayerConstants.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Point;
import Gamestates.Playing;
import audio.AudioPlayer;

import java.awt.geom.Rectangle2D;

import main.Game;
import utilz.LoadSave;
import static utilz.HelpMethods.*;
import static utilz.Constants.*;

public class Player extends Entity {
	private BufferedImage[][] animations;

	private boolean moving = false, attacking = false;
	private boolean left, right, down, jump;

	private int[][] lvlData;
	private float xDrawOffset = 21 * Game.SCALE;
	private float yDrawOffset = 4 * Game.SCALE;
	// Jumping and Gravity
	private float jumpSpeed = -3.0f * Game.SCALE;
	private float fallSpeedAfterCollision = 0.5f;

	// Status Bar ui
	private BufferedImage statusBarImg;
	private int statusBarWidth = (int) (192 * Game.SCALE);
	private int statusBarHeight = (int) (58 * Game.SCALE);
	private int statusBarX = (int) (10 * Game.SCALE);
	private int statusBarY = (int) (10 * Game.SCALE);

	private int healthBarWidth = (int) (150 * Game.SCALE);
	private int healthBarHeight = (int) (4 * Game.SCALE);
	private int healtBarXStart = (int) (34 * Game.SCALE);
	private int healthBarYStart = (int) (14 * Game.SCALE);
	private int healthWidth = healthBarWidth;

	private int powerBarWidth = (int) (104 * Game.SCALE);
	private int powerBarHeight = (int) (2 * Game.SCALE);
	private int powerBarXStart = (int) (44 * Game.SCALE);
	private int powerBarYStart = (int) (34 * Game.SCALE);
	private int powerWidth = powerBarWidth;
	private int powerMaxValue = 200;
	private int powerValue = powerMaxValue;

	private int flipX = 0;
	private int flipW = 1;
	private boolean attackChecked;
	private Playing playing;

	private int tileY = 0;
	private boolean powerAttackActivve;
	private int powerAttakcTick;
	private int powerGrowSpeed = 15;
	private int powerGrowTick;

	public Player(float x, float y, int width, int height, Playing playing) {
		super(x, y, width, height);
		this.playing = playing;
		this.state = IDLE;
		this.maxHealth = 150;
		this.currentHealth = maxHealth;
		this.walkSpeed = Game.SCALE * 1.0f;
		loadAnimations();
		initHitbox(20, 28);
		initAttackBox();
	}

	public void setSpawn(Point spawn) {
		this.x = spawn.x;
		this.y = spawn.y;
		hitbox.x = x;
		hitbox.y = y;
	}

	public void update() {
		updateHealthBar();
		updatePowerBar();
		if (currentHealth <= 0) {
			if (state != DEAD) {
				state = DEAD;
				aniTick = 0;
				aniIndex = 0;
				playing.setPlayerDying(true);
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);
			} else if (aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
				playing.setGameOver(true);
				playing.getGame().getAudioPlayer().stopSongs();
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
			} else {
				updateAnimationTick();
			}
			// playing.setGameOver(true);
			return;
		}
		updateAttackBox();
		updatePos();
		if (moving) {
			checkPotionTouched();
			checkSpikesTouched();
			tileY = (int) (hitbox.y / Game.TILES_SIZE);
			if (powerAttackActivve) {
				powerAttakcTick++;
				if (powerAttakcTick >= 35) {
					powerAttakcTick = 0;
					powerAttackActivve = false;
				}
			}
		}
		if (attacking || powerAttackActivve) {
			checkAttack();
		}
		updateAnimationTick();
		setAnimation();
	}

	private void checkSpikesTouched() {
		playing.checkSpikesTouched(this);
	}

	private void checkPotionTouched() {
		playing.checkPotionTouched(hitbox);
	}

	public void checkAttack() {
		if (attackChecked || aniIndex != 1)
			return;
		attackChecked = true;
		if (powerAttackActivve)
			attackChecked = false;

		playing.checkEnemyHit(attackBox);
		playing.checkObjectHit(attackBox);
		playing.getGame().getAudioPlayer().playAttackSound();
	}

	private void updateAttackBox() {
		if (right && left) {
			if (flipW == 1) {
				attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
			} else {
				attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
			}
		} else if (right || (powerAttackActivve && flipW == 1)) {
			attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
		} else if (left || (powerAttackActivve && flipW == -1)) {
			attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
		}
		attackBox.y = hitbox.y + (Game.SCALE * 10);
	}

	private void initAttackBox() {
		attackBox = new Rectangle2D.Float(x, y, (int) (20 * Game.SCALE), (int) (20 * Game.SCALE));
		resetAttackBox();
	}

	private void updateHealthBar() {
		healthWidth = (int) ((currentHealth / (float) (maxHealth)) * healthBarWidth);
	}

	private void updatePowerBar() {
		powerWidth = (int) ((powerValue / (float) powerMaxValue * powerBarWidth));

		powerGrowTick++;
		if (powerGrowTick >= powerGrowSpeed) {
			powerGrowTick = 0;
			changePower(1);
		}
	}

	public void render(Graphics g, int lvlOffset) {
		g.drawImage(animations[state][aniIndex], (int) (hitbox.x - xDrawOffset) - lvlOffset + flipX,
				(int) (hitbox.y - yDrawOffset), width * flipW, height, null);
		// drawAttackBox(g, lvlOffset);
		drawUI(g);
	}

	private void drawUI(Graphics g) {
		// background UI
		g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);
		// health ui
		g.setColor(Color.RED);
		g.fillRect(healtBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

		// power ui
		g.setColor(Color.yellow);
		g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
	}

	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= ANI_SPEED) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(state)) {
				aniIndex = 0;
				attacking = false;
				attackChecked = false;
			}

		}

	}

	private void setAnimation() {
		int startAni = state;

		if (moving)
			state = RUNNING;
		else
			state = IDLE;
		if (inAir) {
			if (airSpeed < 0) {
				state = JUMP;
			} else {
				state = FALLING;
			}
		}
		if (powerAttackActivve) {
			state = ATTACK;
			aniIndex = 1;
			aniTick = 0;
			return;
		}
		if (attacking) {
			state = ATTACK;
			if (startAni != ATTACK) {
				aniIndex = 1;
				aniTick = 0;
				return;
			}
		}
		if (startAni != state)
			resetAniTick();
	}

	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}

	private void updatePos() {
		moving = false;
		if (jump) {
			jump();
		}
		if (!inAir) {
			if (!powerAttackActivve)
				if ((!left && !right) || (right && left)) {
					return;
				}
		}
		float xSpeed = 0;
		if (left && !right) {
			xSpeed -= walkSpeed;
			flipX = width;
			flipW = -1;
		}
		if (right && !left) {
			xSpeed += walkSpeed;
			flipX = 0;
			flipW = 1;
		}
		if (powerAttackActivve) {
			if ((!left && !right) || (left & right)) {
					if (flipW == -1)
						xSpeed = -walkSpeed;
					else
						xSpeed = walkSpeed;
			}
			xSpeed *= 3;
		}

		if (!inAir) {
			if (!IsEntityOnFloor(hitbox, lvlData)) {
				inAir = true;
			}
		}

		if (inAir && !powerAttackActivve) {
			if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
				hitbox.y += airSpeed;
				airSpeed += GRAVITY;
				updateXPos(xSpeed);
			} else {
				hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
				if (airSpeed > 0) {
					resetInAir();
				} else {
					airSpeed = fallSpeedAfterCollision;
				}
				updateXPos(xSpeed);
			}
		} else {
			updateXPos(xSpeed);
		}
		moving = true;
		// if(CanMoveHere(hitbox.x + xSpeed,hitbox.y + ySpeed, hitbox.width,
		// hitbox.height, lvlData))
		// {
		// hitbox.x += xSpeed;
		// hitbox.y += ySpeed;
		// moving =true;
		// }
	}

	private void jump() {
		if (inAir) {
			return;
		} else {

			playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
			inAir = true;
			airSpeed = jumpSpeed;
		}
	}

	private void resetInAir() {
		inAir = false;
		airSpeed = 0;
	}

	private void updateXPos(float xSpeed) {
		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
			hitbox.x += xSpeed;
		} else {
			hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
			if (powerAttackActivve) {
				powerAttackActivve = false;
				powerAttakcTick = 0;
			}
		}
	}

	private void loadAnimations() {

		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);

		animations = new BufferedImage[7][8];
		for (int j = 0; j < animations.length; j++)
			for (int i = 0; i < animations[j].length; i++)
				animations[j][i] = img.getSubimage(i * 64, j * 40, 64, 40);

		statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
	}

	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData)) {
			inAir = true;
		}
	}

	public void resetDirBooleans() {
		left = false;
		right = false;

		down = false;
	}

	public void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public boolean isDown() {
		return down;
	}

	public void setDown(boolean down) {
		this.down = down;
	}

	public void setJump(boolean jump) {
		this.jump = jump;
	}

	public void resetAll() {
		resetDirBooleans();
		inAir = false;
		attacking = false;
		moving = false;
		airSpeed = 0f;
		state = IDLE;
		currentHealth = maxHealth;

		hitbox.x = x;
		hitbox.y = y;

		resetAttackBox();
		if (!IsEntityOnFloor(hitbox, lvlData)) {
			inAir = true;
		}
	}
	private void resetAttackBox(){
		if (flipW == 1) {
			attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
		} else {
			attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
		}
	}

	public void changePower(int value) {
		powerValue += value;
		if (powerValue >= powerMaxValue) {
			powerValue = powerMaxValue;
		} else if (powerValue <= 0) {
			powerValue = 0;
		}
	}

	public void changeHealth(int value) {
		currentHealth += value;
		if (currentHealth <= 0) {
			currentHealth = 0;
			// gameOver();P
		} else if (currentHealth >= maxHealth) {
			currentHealth = maxHealth;
		}

	}

	public void kill() {
		currentHealth = 0;
	}

	public int getTiley() {
		return tileY;
	}

	public void powerAttack() {
		if (powerAttackActivve)
			return;
		if (powerValue >= 60) {
			powerAttackActivve = true;
			changePower(-60);
		}
	}
}
