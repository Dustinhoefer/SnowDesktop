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
	private final ArrayList<Snowflake> snowflakes[][];
	private boolean debug;

	private final ArrayList<Snowflake> snowflakeList = new ArrayList<Snowflake>();
	// Queue to add new snowflakes
	private final Queue<Snowflake> snowflakeAddQueue = new ConcurrentLinkedQueue<Snowflake>();
	// Queue to delete old snowflakes
	private final Queue<Snowflake> snowflakeDeleteQueue = new ConcurrentLinkedQueue<Snowflake>();

	private final RECT rect;

	public SnowflakePanel(final RECT currentScreenRect) {
		System.out.println(currentScreenRect);
		this.sizeX = currentScreenRect.right - currentScreenRect.left;
		this.sizeY = currentScreenRect.bottom - currentScreenRect.top;
		this.rect = currentScreenRect;
		this.setSize(this.sizeX, this.sizeY);
		this.snowflakes = new ArrayList[this.sizeX][this.sizeY];
		this.setFocusable(false);

		// This thread generates new snowflakes
		final Thread snowflakeGenerationThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					try {
						SnowflakePanel.this.generateSnowflakes();
					} catch (final InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		snowflakeGenerationThread.start();

		// This timer is used to repaint this panel every 10 ms and show the snowflakes
		final Timer swingAnimator = new Timer(10, new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				try {
					SnowflakePanel.this.updateSnowflakes();
				} catch (final InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				SnowflakePanel.this.paint();
			}

		});
		swingAnimator.setRepeats(true);
		swingAnimator.start();

	}

	@Override
	public void paint(final Graphics g) {

		// Every snowflake is checked if it has fallen on a visible window or the taskbar
		// If that is the case, the snowflake will be frozen
		// If not, the position will be updated
		for (final Snowflake snowflake : this.snowflakeList) {

			final boolean fallen = snowflake.hasFallen();

			int x = (int) snowflake.getCurrentPositionX();
			int y = (int) snowflake.getCurrentPositionY();

			if (!fallen) {
				if (this.snowflakes[x][y] != null) {
					this.snowflakes[x][y].remove(snowflake);
					if (this.snowflakes[x][y].size() == 0) {
						this.snowflakes[x][y] = null;
					}
				}

				snowflake.update();

				x = (int) snowflake.getCurrentPositionX();
				y = (int) snowflake.getCurrentPositionY();

				// Move Snowflakes that are behind a window up to the windows edge; not fully implemented yet
				if (x >= 0 && x < this.sizeX) {
					if (y >= 0 && y < this.sizeY) {

						boolean changed = false;

						if (this.rect.left == -1920) {
							while (y > 0 && SnowSimulator.getWindows()[x][y] > 0) {
								y = y - 1;
								changed = true;
							}
						} else if (this.rect.left == 0) {
							while (y > 0 && SnowSimulator.getWindows()[x + 1920][y] > 0) {
								y = y - 1;
								changed = true;
							}
						} else {
							while (y > 0 && SnowSimulator.getWindows()[x + (1920 * 2)][y] > 0) {
								y = y - 1;
								changed = true;
							}
						}

						if (changed) {
							y = y + 1;
							snowflake.setPosY(y);
							snowflake.setFallen(true);
						}

						this.addSnowflake(x, y, snowflake);
					}
				}
			} else {
				int index = 0;
				if (x >= 0 && x < this.sizeX) {
					if (y >= 0 && y < this.sizeY) {
						if (this.snowflakes[x][y] != null) {
							final int countBefore = this.snowflakes[x][y].size();
							while (this.snowflakes[x][y].size() > 1 && index < this.snowflakes[x][y].size()) {
								if (snowflake != this.snowflakes[x][y].get(index) && this.snowflakes[x][y].get(index).hasFallen() && !this.snowflakes[x][y].get(index).isToDelete()) {
									snowflake.addWeight(this.snowflakes[x][y].get(index).getWeight());
									this.snowflakes[x][y].get(index).setWeight(0);
									this.snowflakes[x][y].get(index).setToDelete(true);
									this.snowflakeDeleteQueue.add(this.snowflakes[x][y].get(index));
									this.snowflakes[x][y].remove(index);
								}
								index++;
							}
							if (countBefore != this.snowflakes[x][y].size()) {
								System.out.println("Count difference: " + (countBefore - this.snowflakes[x][y].size()));
							}
						}
					}
				}
			}

			if (x < this.sizeX && y < this.sizeY - this.taskBarHeight) {
				if (x >= 0) {
					// Inside windowrect
					if (this.rect.left == -1920) {
						if (SnowSimulator.getWindows()[x][y] > 0) {
							snowflake.setFallen(true);
						}
						// outside windowrect
						else {
							snowflake.setFallen(false);
						}
					} else if (this.rect.left == 0) {
						if (SnowSimulator.getWindows()[x + 1920][y] > 0) {
							snowflake.setFallen(true);
						}
						// outside windowrect
						else {
							snowflake.setFallen(false);
						}
					} else {
						if (SnowSimulator.getWindows()[x + (1920 * 2)][y] > 0) {
							snowflake.setFallen(true);
						}
						// outside windowrect
						else {
							snowflake.setFallen(false);
						}
					}
				} else {
					snowflake.setFallen(true);
				}
			} else {
				if (y > this.sizeY - this.taskBarHeight) {
					if (x >= 0 && x < this.sizeX) {
						if (y >= 0 && y < this.sizeY) {
							this.snowflakes[x][(int) snowflake.getCurrentPositionY()].remove(snowflake);
						}
						snowflake.setPosY(this.sizeY - this.taskBarHeight);
						this.addSnowflake(x, this.sizeY - this.taskBarHeight, snowflake);
					}
				}

				snowflake.setFallen(true);
			}
		}

		while (!this.snowflakeDeleteQueue.isEmpty()) {
			this.snowflakeList.remove(this.snowflakeDeleteQueue.poll());
		}

		// Clear the screen
		((Graphics2D) g).setBackground(new Color(255, 255, 255, 0));
		g.clearRect(0, 0, this.sizeX, this.sizeY);

		// Draw the snowflakes
		for (int x = 0; x < this.sizeX; x++) {
			for (int y = 0; y < this.sizeY; y++) {
				if (this.snowflakes[x][y] != null && this.snowflakes[x][y].size() > 0) {
					int value = 100 + (this.snowflakes[x][y].size() - 1) * 25;
					if (value > 255) {
						value = 255;
					}
					g.setColor(new Color(255, 255, 255, value));
					g.fillRect(x - Snowflake.size, y - Snowflake.size, Snowflake.size, Snowflake.size);
				}
				if (this.debug) {

					if (this.rect.left == -1920) {
						if (SnowSimulator.getWindows()[x][y] > 0) {
							g.setColor(Color.green);
							g.fillRect(x, y, 1, 1);
						}
					} else if (this.rect.left == 0) {
						if (SnowSimulator.getWindows()[x + 1920][y] > 0) {
							g.setColor(Color.green);
							g.fillRect(x, y, 1, 1);
						}
					} else {
						if (SnowSimulator.getWindows()[x + (1920 * 2)][y + this.rect.top] > 0) {
							g.setColor(Color.green);
							g.fillRect(x, y, 1, 1);
						}
					}

				}
			}
		}
	}

	private void addSnowflake(final int x, final int y, final Snowflake snowflake) {
		if (this.snowflakes[x][y] == null) {
			this.snowflakes[x][y] = new ArrayList<Snowflake>();
		}
		this.snowflakes[x][y].add(snowflake);
	}

	public void paint() {
		this.repaint();
		// SnowflakePanel.instance.paint(SnowflakePanel.instance.getGraphics());
	}

	public void setDebug(final boolean debugMode) {
		this.debug = debugMode;
	}

	public void addSnowflake() {
		final int posX = (int) (Math.random() * (this.sizeX));
		final Snowflake snowflake = new Snowflake(posX);
		this.snowflakeAddQueue.add(snowflake);
	}

	public void updateSnowflakes() throws InterruptedException {
		while (!this.snowflakeAddQueue.isEmpty()) {
			final Snowflake snowflake = this.snowflakeAddQueue.poll();
			this.snowflakeList.add(snowflake);
		}
	}

	public void generateSnowflakes() throws InterruptedException {
		final long start = System.currentTimeMillis();
		for (int i = 0; i < SnowflakePanel.snowFlakeAmount; i++) {
			this.addSnowflake();
		}
		final int value = (int) ((Math.random() * 500) + 250 - (System.currentTimeMillis() - start));
		if (value > 0) {
			Thread.sleep(value);
		}
	}
}
