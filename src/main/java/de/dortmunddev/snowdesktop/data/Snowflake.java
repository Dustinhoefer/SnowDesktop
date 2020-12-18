package de.dortmunddev.snowdesktop.data;

public class Snowflake {

	private float weight;

	private boolean fallenToGround;

	public boolean isFallenToGround() {
		return fallenToGround;
	}

	public void setFallenToGround(boolean fallenToGround) {
		this.fallenToGround = fallenToGround;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	private float fallingDirectionX;

	public float getFallingDirectionX() {
		return fallingDirectionX;
	}

	public void setFallingDirectionX(float fallingDirectionX) {
		this.fallingDirectionX = fallingDirectionX;
	}

	private float currentPositionX, currentPositionY;
	private boolean fallen;
	private boolean isToDelete;

	public Snowflake(final int currentPositionX) {
		this.currentPositionX = currentPositionX;
		this.currentPositionY = -50;

		this.fallingDirectionX = (float) Math.random() * 0.4f;

		if (Math.random() < 0.5f) {
			this.fallingDirectionX *= -1;
		}

		this.weight = 0.5f + (float) (Math.random() * 0.5);
	}

	public void update() {
		// Update the position of this snowflake according to its weight and falling
		// direction
		this.currentPositionX += this.fallingDirectionX;
		this.currentPositionY += this.weight;
	}

	public float getCurrentPositionX() {
		return currentPositionX;
	}

	public float getCurrentPositionY() {
		return currentPositionY;
	}

	public void setFallen(final boolean fallen) {
		this.fallen = fallen;
	}

	public float getWeight() {
		return this.weight;
	}

	public boolean hasFallen() {
		return this.fallen;
	}

	public void addWeight(final float weight) {
		if (this.weight < 15) {
			this.weight += weight;
			if (this.weight > 15) {
				this.weight = 15;
			}
		}
	}

	public void setWeight(final int weight) {
		this.weight = weight;
	}

	public boolean isToDelete() {
		return this.isToDelete;
	}

	public void setToDelete(final boolean isToDelete) {
		this.isToDelete = isToDelete;
	}

	public void setPosY(final int y) {
		this.currentPositionY = y;
	}

	public void setPosX(final int x) {
		this.currentPositionX = x;
	}

	public void changeDirection() {
		this.fallingDirectionX *= -1;
	}
}