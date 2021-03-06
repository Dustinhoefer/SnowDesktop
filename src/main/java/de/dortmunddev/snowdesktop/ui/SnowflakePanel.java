package de.dortmunddev.snowdesktop.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.sun.jna.platform.win32.WinDef.RECT;

import de.dortmunddev.snowdesktop.data.Snowflake;
import de.dortmunddev.snowdesktop.logic.SnowSimulator;

//used to paint all the snowflakes of a specific monitor
public class SnowflakePanel extends JPanel {

	// Amount of snowflakes that are generated per tick
	public static int snowFlakeAmount = 0;

	private final Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
	private final Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

	private final int taskBarHeight = this.scrnSize.height - this.winSize.height;
	private final int taskBarWidth = this.scrnSize.width - this.winSize.width;

	private static final long serialVersionUID = 1L;
	private int sizeX = 0;
	private int sizeY = 0;
	private boolean debug;

//	private final ArrayList<Snowflake> snowflakes[][];

	// List for all snowflakes
	private final ArrayList<Snowflake> snowflakeList = new ArrayList<Snowflake>();

	// Queue to add new snowflakes
	private final Queue<Snowflake> snowflakeAddQueue = new ConcurrentLinkedQueue<Snowflake>();

	// Queue to delete old snowflakes
	private final Queue<Snowflake> snowflakeDeleteQueue = new ConcurrentLinkedQueue<Snowflake>();

	private ArrayList<Snowflake>[] snowflakesTaskbar;

	private final RECT rect;

	public SnowflakePanel(final RECT currentScreenRect) {
		this.sizeX = currentScreenRect.right - currentScreenRect.left;
		this.sizeY = currentScreenRect.bottom - currentScreenRect.top;
		this.rect = currentScreenRect;
		this.setSize(sizeX, sizeY);
		snowflakesTaskbar = new ArrayList[sizeX];

		for (int i = 0; i < snowflakesTaskbar.length; i++) {
			snowflakesTaskbar[i] = new ArrayList<>();
		}

//		this.snowflakes = new ArrayList[this.sizeX][this.sizeY];
		this.setFocusable(false);

		// This thread generates new snowflakes
		final Thread snowflakeGenerationThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						generateNewSnowflakes();
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		snowflakeGenerationThread.start();

		// This timer is used to repaint this panel every 10 ms and show the snowflakes
		final Timer swingAnimator = new Timer(10, new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					addNewSnowflakesFromQueue();
				} catch (final InterruptedException e1) {
					e1.printStackTrace();
				}

				// Update snow flakes
				updateAllSnowflakes();

				repaint();
			}

		});
		swingAnimator.setRepeats(true);
		swingAnimator.start();
	}

	private void updateAllSnowflakes() {
		// Every snowflake is checked if it has fallen on a visible window or the
		// taskbar
		// If that is the case, the snowflake will be frozen
		// If not, the position will be updated
		for (final Snowflake snowflake : this.snowflakeList) {

			float x = snowflake.getCurrentPositionX();
			float y = snowflake.getCurrentPositionY();

			if (!snowflake.hasFallen() && !snowflake.isFallenToGround()) {
				snowflake.update();

				x = snowflake.getCurrentPositionX();
				y = snowflake.getCurrentPositionY();

				int posX = (int) x;

				if (posX < 0) {
					posX = 0;
					snowflake.changeDirection();
				} else if (posX >= sizeX) {
					posX = sizeX - 1;
					snowflake.changeDirection();
				}

				if (y > this.sizeY - this.taskBarHeight - snowflakesTaskbar[posX].size()) {
					snowflake.setPosY(this.sizeY - this.taskBarHeight - snowflakesTaskbar[posX].size());
					snowflake.setFallenToGround(true);
					snowflakesTaskbar[posX].add(snowflake);
				} else {
					if (SnowSimulator.collisionMap != null) {
						if (x > 0 && y > 0) {
							if (SnowSimulator.collisionMap[(int) x][(int) y]) {
								snowflake.setFallen(true);
								System.out.println("lol");
							}
						}
					}
				}

				// Move Snowflakes that are behind a window up to the windows edge
			} else {
				if (!snowflake.isFallenToGround()) {
					if (SnowSimulator.collisionMap[(int) x][(int) (y + 1)] == false) {
						snowflake.setFallen(false);
						snowflake.setFallingDirectionX((float) (Math.random() * 0.1f));
						snowflake.setWeight((float) (0.7f + Math.random() * 0.3f));
					}
				}

//				int index = 0;
//				if (x >= 0 && x < this.sizeX) {
//					if (y >= 0 && y < this.sizeY) {
//						if (this.snowflakes[x][y] != null) {
//							final int countBefore = this.snowflakes[x][y].size();
//							while (this.snowflakes[x][y].size() > 1 && index < this.snowflakes[x][y].size()) {
//								if (snowflake != this.snowflakes[x][y].get(index)
//										&& this.snowflakes[x][y].get(index).hasFallen()
//										&& !this.snowflakes[x][y].get(index).isToDelete()) {
//									snowflake.addWeight(this.snowflakes[x][y].get(index).getWeight());
//									this.snowflakes[x][y].get(index).setWeight(0);
//									this.snowflakes[x][y].get(index).setToDelete(true);
//									this.snowflakeDeleteQueue.add(this.snowflakes[x][y].get(index));
//									this.snowflakes[x][y].remove(index);
//								}
//								index++;
//							}
//							if (countBefore != this.snowflakes[x][y].size()) {
//								System.out.println("Count difference: " + (countBefore - this.snowflakes[x][y].size()));
//							}
//						}
//					}
//				}
			}

//			if (x < this.sizeX && y < this.sizeY - this.taskBarHeight) {
//				if (x >= 0) {
//					// Inside windowrect
//					if (this.rect.left == -1920) {
//						if (SnowSimulator.getWindows()[x][y] > 0) {
//							snowflake.setFallen(true);
//						}
//						// outside windowrect
//						else {
//							snowflake.setFallen(false);
//						}
//					} else if (this.rect.left == 0) {
//						if (SnowSimulator.getWindows()[x + 1920][y] > 0) {
//							snowflake.setFallen(true);
//						}
//						// outside windowrect
//						else {
//							snowflake.setFallen(false);
//						}
//					} else {
//						if (SnowSimulator.getWindows()[x + (1920 * 2)][y] > 0) {
//							snowflake.setFallen(true);
//						}
//						// outside windowrect
//						else {
//							snowflake.setFallen(false);
//						}
//					}
//				} else {
//					snowflake.setFallen(true);
//				}
//			} else {
//				if (y > this.sizeY - this.taskBarHeight) {
//					if (x >= 0 && x < this.sizeX) {
//						if (y >= 0 && y < this.sizeY) {
//							this.snowflakes[x][(int) snowflake.getCurrentPositionY()].remove(snowflake);
//						}
//						snowflake.setPosY(this.sizeY - this.taskBarHeight);
//						this.addSnowflake(x, this.sizeY - this.taskBarHeight, snowflake);
//					}
//				}
//
//				snowflake.setFallen(true);
//			}
		}

//		while (!this.snowflakeDeleteQueue.isEmpty()) {
//			this.snowflakeList.remove(this.snowflakeDeleteQueue.poll());
//		}
	}

	@Override
	public void paint(final Graphics g) {
		// Clear the screen
		((Graphics2D) g).setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, this.sizeX, this.sizeY);

		for (Snowflake snowflake : snowflakeList) {
			if (!snowflake.isFallenToGround() && snowflake.getCurrentPositionY() > 0) {
				g.setColor(new Color(255, 255, 255, 255));
				g.fillRect((int) snowflake.getCurrentPositionX(), (int) snowflake.getCurrentPositionY(), 1, 1);
			}
		}

		for (int x = 0; x < snowflakesTaskbar.length; x++) {
			g.setColor(new Color(255, 255, 255, 255));
			g.fillRect(x, this.sizeY - this.taskBarHeight, 1, -snowflakesTaskbar[x].size());
		}

//		for (int x = 0; x < sizeX; x++) {
//			for (int y = 0; y < sizeY; y++) {
//				if (SnowSimulator.collisionMap != null) {
//					if (SnowSimulator.collisionMap[x][y]) {
//						g.setColor(Color.GREEN);
//						g.fillRect(x, y, 1, 1);
//					}
//				}
//			}
//		}

		// Draw the snowflakes
//		for (int x = 0; x < this.sizeX; x++) {
//			for (int y = 0; y < this.sizeY; y++) {
//				if (this.snowflakes[x][y] != null && this.snowflakes[x][y].size() > 0) {
//					int value = 100 + (this.snowflakes[x][y].size() - 1) * 25;
//					if (value > 255) {
//						value = 255;
//					}
//					g.setColor(new Color(255, 255, 255, value));
////					g.fillRect(x - Snowflake.getSize(), y - Snowflake.getSize(), Snowflake.getSize(),
////							Snowflake.getSize());
//					g.fillRect(x, y, 1, 1);
//				}
//
////				if (this.debug) {
////					if (this.rect.left == -1920) {
////						if (SnowSimulator.getWindows()[x][y] > 0) {
////							g.setColor(Color.green);
////							g.fillRect(x, y, 1, 1);
////						}
////					} else if (this.rect.left == 0) {
////						if (SnowSimulator.getWindows()[x + 1920][y] > 0) {
////							g.setColor(Color.green);
////							g.fillRect(x, y, 1, 1);
////						}
////					} else {
////						if (SnowSimulator.getWindows()[x + (1920 * 2)][y + this.rect.top] > 0) {
////							g.setColor(Color.green);
////							g.fillRect(x, y, 1, 1);
////						}
////					}
////				}
//			}
//		}
	}

//	private void addSnowflake(final int x, final int y, final Snowflake snowflake) {
//		if (this.snowflakes[x][y] == null) {
//			this.snowflakes[x][y] = new ArrayList<Snowflake>();
//		}
//
//		this.snowflakes[x][y].add(snowflake);
//	}

	public void setDebug(final boolean debugMode) {
		this.debug = debugMode;
	}

	public void addNewSnowflakesFromQueue() throws InterruptedException {
		while (!this.snowflakeAddQueue.isEmpty()) {
			final Snowflake snowflake = this.snowflakeAddQueue.poll();
			this.snowflakeList.add(snowflake);
		}
	}

	public void generateNewSnowflakes() throws InterruptedException {
		final long start = System.currentTimeMillis();

		for (int i = 0; i < SnowflakePanel.snowFlakeAmount; i++) {
			final int posX = (int) (Math.random() * (this.sizeX));

			final Snowflake snowflake = new Snowflake(posX);
			this.snowflakeAddQueue.add(snowflake);
		}

		final int value = (int) ((Math.random() * 500) + 250 - (System.currentTimeMillis() - start));

		if (value > 0) {
			Thread.sleep(value);
		}
	}
}
