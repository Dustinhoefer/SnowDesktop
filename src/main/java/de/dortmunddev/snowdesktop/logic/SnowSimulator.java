package de.dortmunddev.snowdesktop.logic;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.WINDOWINFO;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;

import de.dortmunddev.snowdesktop.data.WindowHandle;
import de.dortmunddev.snowdesktop.ui.CustomTrayIcon;
import de.dortmunddev.snowdesktop.ui.SnowWindow;
import de.dortmunddev.snowdesktop.ui.SnowflakePanel;

public class SnowSimulator {

	private static final ArrayList<SnowWindow> desktops = new ArrayList<SnowWindow>();

	// Rect of the whole monitor setup
	private final static WinDef.RECT totalScreenRect = new WinDef.RECT();

	// Already known windows
	private final ArrayList<WindowHandle> windowList = new ArrayList<WindowHandle>();

	private RECT cursorRect = new RECT();

	// All occupied pixels by ALL visible windows
	private static int[][] windows;

	public static boolean[][] collisionMap;

	public SnowSimulator() {
		// First create tray icon
		final CustomTrayIcon trayIconCustom = new CustomTrayIcon();
		trayIconCustom.setTrayIcon();

//		// Initialize; get resolution of all monitors and the total resolution
		GraphicsDevice[] monitorData = getMonitorSizes();

		for (GraphicsDevice graphicsDevice : monitorData) {
			Rectangle currentMonitorBounds = graphicsDevice.getDefaultConfiguration().getBounds();

			int currentMonitorHeight = graphicsDevice.getDisplayMode().getHeight();

			float scaling = (float) currentMonitorHeight / (float) currentMonitorBounds.height;

			if (scaling != 1.0f) {
				currentMonitorBounds.setBounds((int) (currentMonitorBounds.x * scaling),
						(int) (currentMonitorBounds.y * scaling), (int) (currentMonitorBounds.width * scaling),
						(int) (currentMonitorBounds.height * scaling));
				System.out.println("Scaled: " + currentMonitorBounds);
			}

			if (currentMonitorBounds.getX() < SnowSimulator.totalScreenRect.left) {
				SnowSimulator.totalScreenRect.left = (int) currentMonitorBounds.getX();
			}

			if (currentMonitorBounds.getY() < SnowSimulator.totalScreenRect.top) {
				SnowSimulator.totalScreenRect.top = (int) currentMonitorBounds.getY();
			}

			if ((int) currentMonitorBounds.getX()
					+ (int) currentMonitorBounds.getWidth() > SnowSimulator.totalScreenRect.right) {
				SnowSimulator.totalScreenRect.right = (int) currentMonitorBounds.getX()
						+ (int) currentMonitorBounds.getWidth();
			}

			if ((int) currentMonitorBounds.getY()
					+ (int) currentMonitorBounds.getHeight() > SnowSimulator.totalScreenRect.bottom) {
				SnowSimulator.totalScreenRect.bottom = (int) currentMonitorBounds.getY()
						+ (int) currentMonitorBounds.getHeight();
			}

			final WinDef.RECT currentMonitorRect = new WinDef.RECT();
			currentMonitorRect.left = (int) currentMonitorBounds.getX();
			currentMonitorRect.right = (int) currentMonitorBounds.getX() + (int) currentMonitorBounds.getWidth();
			currentMonitorRect.top = (int) currentMonitorBounds.getY();
			currentMonitorRect.bottom = (int) currentMonitorBounds.getY() + (int) currentMonitorBounds.getHeight();

			SnowSimulator.getDesktops().add(new SnowWindow(currentMonitorRect));
		}

		collisionMap = new boolean[SnowSimulator.getTotalScreenRect().left * -1
				+ SnowSimulator.getTotalScreenRect().right][SnowSimulator.getTotalScreenRect().bottom];
		System.out.println("CollisionMapSize: " + collisionMap.length + "/" + collisionMap[0].length);

		for (final SnowWindow snowDesktop : SnowSimulator.getDesktops()) {
			System.out.println(snowDesktop.getScreenRect());
		}

		// Create an array for the windows
		SnowSimulator.setWindows(new int[SnowSimulator.totalScreenRect.right
				- SnowSimulator.totalScreenRect.left][SnowSimulator.totalScreenRect.bottom
						- SnowSimulator.totalScreenRect.top]); // 23MB
	}

	// Starts a thread which keeps track of all visible windows
	public void start() {

		final Thread windowUpdater = new Thread(new Runnable() {

			@Override
			public void run() {
//				try {
//					SnowSimulator.this.getAllWindows();
//				} catch (final InterruptedException e) {
//					e.printStackTrace();
//				}

				while (true) {
//					try {
//						SnowSimulator.this.updateWindows();
//					} catch (final InterruptedException e) {
//						e.printStackTrace();
//					}
					handleCursorPosition();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		});

		windowUpdater.setDaemon(true);
		windowUpdater.start();
	}

	// gets all the visible windows and adds them to the windowList
	public void getAllWindows() throws InterruptedException {

		for (final WindowHandle curWindow : SnowSimulator.this.windowList) {
			curWindow.setStillExists(false);
		}

		User32.EnumWindows(new WNDENUMPROC() {
			@Override
			public boolean callback(final HWND arg0, final Pointer arg1) {
				final byte[] buffer = new byte[1024];
				User32.GetWindowTextA(arg0, buffer, buffer.length);
				final String title = Native.toString(buffer);

				final WinDef.RECT rect = new WinDef.RECT();
				User32.GetWindowRect(arg0, rect);

				if (User32.IsWindowVisible(arg0) && !title.equals("") && !title.equals("Program Manager")) {
					boolean alreadyFound = false;

					for (final WindowHandle curWindow : SnowSimulator.this.windowList) {
						if (arg0.getPointer().equals(curWindow.getWindowHandle().getPointer())) {
							alreadyFound = true;
							curWindow.setStillExists(true);
							break;
						}
					}
					if (!alreadyFound) {
						SnowSimulator.this.windowList.add(new WindowHandle(arg0, title, rect));
						for (int x = rect.left; x < rect.right; x++) {
							for (int y = rect.top; y < rect.bottom; y++) {
								if (x - SnowSimulator.totalScreenRect.left > 0
										&& x - SnowSimulator.totalScreenRect.left < SnowSimulator.getWindows().length
										&& y > 0 && y < SnowSimulator.getWindows()[0].length - 2) {
									SnowSimulator.getWindows()[x
											- SnowSimulator.totalScreenRect.left][y] = SnowSimulator.getWindows()[x
													- SnowSimulator.totalScreenRect.left][y] + 1;
								}
							}
						}
					} else {

					}
				}
				return true;
			}
		}, 0);

		final Queue<WindowHandle> removeWindowQueue = new ConcurrentLinkedQueue<WindowHandle>();

		for (final WindowHandle curWindow : SnowSimulator.this.windowList) {
			if (curWindow.stillExists() == false) {
				removeWindowQueue.add(curWindow);
			}
		}

		while (!removeWindowQueue.isEmpty()) {
			final WindowHandle curWindow = removeWindowQueue.poll();

			SnowSimulator.this.windowList.remove(curWindow);

			final WinDef.RECT rect = curWindow.getRect();

			for (int x = rect.left; x < rect.right; x++) {
				for (int y = rect.top; y < rect.bottom; y++) {
					if (x - SnowSimulator.totalScreenRect.left > 0
							&& x - SnowSimulator.totalScreenRect.left < SnowSimulator.getWindows().length && y > 0
							&& y < SnowSimulator.getWindows()[0].length - 2) {
						SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator
								.getWindows()[x - SnowSimulator.totalScreenRect.left][y] - 1;
					}
				}
			}
		}

	}

	// updates the position and size of all visible windows
	public void updateWindows() throws InterruptedException {
		final long start = System.currentTimeMillis();
		final Queue<WindowHandle> toUpdate = new ConcurrentLinkedQueue<WindowHandle>();

		// Check for a changed Window position/size
		for (final WindowHandle curWindow : SnowSimulator.this.windowList) {
			final WinDef.RECT curRect = curWindow.getRect();

			final WinDef.RECT rect = new WinDef.RECT();
			User32.GetWindowRect(curWindow.getWindowHandle(), rect);

			if (rect.left != curRect.left || rect.right != curRect.right || rect.top != curRect.top
					|| rect.bottom != curRect.bottom) {
				toUpdate.add(curWindow);
			}
		}

		for (final WindowHandle curWindow : toUpdate) {
			final WinDef.RECT newRect = new WinDef.RECT();

			User32.GetWindowRect(curWindow.getWindowHandle(), newRect);
			for (int x = newRect.left; x < newRect.right; x++) {
				for (int y = newRect.top; y < newRect.bottom; y++) {
					if (x - SnowSimulator.totalScreenRect.left >= 0
							&& x - SnowSimulator.totalScreenRect.left < SnowSimulator.getWindows().length && y >= 0
							&& y < SnowSimulator.getWindows()[0].length) {
						SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator
								.getWindows()[x - SnowSimulator.totalScreenRect.left][y] + 1;
					}
				}
			}

			final WinDef.RECT rect = curWindow.getRect();
			for (int x = rect.left; x < rect.right; x++) {
				for (int y = rect.top; y < rect.bottom; y++) {
					if (x - SnowSimulator.totalScreenRect.left >= 0
							&& x - SnowSimulator.totalScreenRect.left < SnowSimulator.getWindows().length && y >= 0
							&& y < SnowSimulator.getWindows()[0].length) {
						SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator
								.getWindows()[x - SnowSimulator.totalScreenRect.left][y] - 1;
					}
				}
			}

			SnowSimulator.this.windowList.get(SnowSimulator.this.windowList.indexOf(curWindow)).setRect(newRect);
		}

		this.getAllWindows();

		handleCursorPosition();

		final int value = (int) (16 - (System.currentTimeMillis() - start));
		if (value > 0) {
			Thread.sleep(value);
		}

		Thread.sleep(1000);

	}

	private void handleCursorPosition() {
		for (int x = this.cursorRect.left; x < this.cursorRect.right; x++) {
			for (int y = this.cursorRect.top; y < this.cursorRect.bottom; y++) {
				collisionMap[2560 + x][y] = false;
			}
		}

		final int currentCursorPosX = MouseInfo.getPointerInfo().getLocation().x;
		final int currentCursorPosY = MouseInfo.getPointerInfo().getLocation().y;

		// Check if the snow is falling on the cursor
		for (int x = currentCursorPosX; x < currentCursorPosX + 12; x++) {
			for (int y = currentCursorPosY; y < currentCursorPosY + 16; y++) {
				if (x - SnowSimulator.totalScreenRect.left < SnowSimulator.getWindows().length
						&& y < SnowSimulator.getWindows()[0].length) {
					SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator.getWindows()[x
							- SnowSimulator.totalScreenRect.left][y] + 1;
				}
			}
		}

		for (int x = this.cursorRect.left; x < this.cursorRect.right; x++) {
			for (int y = this.cursorRect.top; y < this.cursorRect.bottom; y++) {
				if (x < SnowSimulator.getWindows().length && y < SnowSimulator.getWindows()[0].length) {
					SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator.getWindows()[x
							- SnowSimulator.totalScreenRect.left][y] - 1;
				}
			}
		}

		this.cursorRect.left = currentCursorPosX;
		this.cursorRect.right = currentCursorPosX + 12;
		this.cursorRect.top = currentCursorPosY;
		this.cursorRect.bottom = currentCursorPosY + 16;

		for (int x = this.cursorRect.left; x < this.cursorRect.right; x++) {
			for (int y = this.cursorRect.top; y < this.cursorRect.bottom; y++) {
				collisionMap[2560 + x][y] = true;
			}
		}
	}

	// Native code for JNA functions (user32)
	static class User32 {
		static {
			Native.register("user32");
		}

		static native boolean EnumWindows(WNDENUMPROC wndenumproc, int lParam);

		static native void GetWindowTextA(HWND hWnd, byte[] buffer, int buflen);

		static native boolean GetWindowInfo(HWND hWnd, WINDOWINFO lpwndpl);

		static native boolean GetWindowRect(WinDef.HWND hWnd, WinDef.RECT rect);

		static native boolean IsWindowVisible(WinDef.HWND hWnd);

	}

	public static void setSnowIntensity(final int value) {
		SnowflakePanel.snowFlakeAmount = value;
	}

	public static int[][] getWindows() {
		return SnowSimulator.windows;
	}

	public static void setWindows(final int[][] windows) {
		SnowSimulator.windows = windows;
	}

	public static RECT getTotalScreenRect() {
		return SnowSimulator.totalScreenRect;
	}

	public static ArrayList<SnowWindow> getDesktops() {
		return SnowSimulator.desktops;
	}

	private static GraphicsDevice[] getMonitorSizes() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		return gs;
	}

}
