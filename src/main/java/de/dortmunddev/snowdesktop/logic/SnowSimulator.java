package de.dortmunddev.snowdesktop.logic;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
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
import de.dortmunddev.snowdesktop.ui.SnowWindow;
import de.dortmunddev.snowdesktop.ui.SnowflakePanel;

public class SnowSimulator {

	private static final ArrayList<SnowWindow> desktops = new ArrayList<SnowWindow>();

	// Rect of the whole monitor setup
	private final static WinDef.RECT totalScreenRect = new WinDef.RECT();

	// Space of the cursor
	private final WinDef.RECT cursorRect = new WinDef.RECT();

	// Already known windows
	private final ArrayList<WindowHandle> windowList = new ArrayList<WindowHandle>();

	// All occupied pixels by ALL visible windows
	private static int[][] windows;

	public SnowSimulator() {

		// Initialize; get resolution of all monitors and the total resolution
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice gs[] = ge.getScreenDevices();
		// for (final GraphicsDevice screen : gs) {
		//
		// if (screen.getDefaultConfiguration().getBounds().x < SnowSimulator.totalScreenRect.left) {
		// SnowSimulator.totalScreenRect.left = screen.getDefaultConfiguration().getBounds().x;
		// }
		// if (screen.getDefaultConfiguration().getBounds().y < SnowSimulator.totalScreenRect.top) {
		// SnowSimulator.totalScreenRect.top = screen.getDefaultConfiguration().getBounds().y;
		// }
		//
		// if (screen.getDefaultConfiguration().getBounds().x + screen.getDefaultConfiguration().getBounds().width > SnowSimulator.totalScreenRect.right) {
		// SnowSimulator.totalScreenRect.right = screen.getDefaultConfiguration().getBounds().x + screen.getDefaultConfiguration().getBounds().width;
		// }
		// if (screen.getDefaultConfiguration().getBounds().y + screen.getDefaultConfiguration().getBounds().height > SnowSimulator.totalScreenRect.bottom) {
		// SnowSimulator.totalScreenRect.bottom = screen.getDefaultConfiguration().getBounds().y + screen.getDefaultConfiguration().getBounds().height;
		// }
		//
		// final WinDef.RECT currentMonitorRect = new WinDef.RECT();
		// currentMonitorRect.left = screen.getDefaultConfiguration().getBounds().x;
		// currentMonitorRect.right = screen.getDefaultConfiguration().getBounds().x + screen.getDefaultConfiguration().getBounds().width;
		// currentMonitorRect.top = screen.getDefaultConfiguration().getBounds().y;
		// currentMonitorRect.bottom = screen.getDefaultConfiguration().getBounds().y + screen.getDefaultConfiguration().getBounds().height;
		//
		// SnowSimulator.getDesktops().add(new SnowDesktop(currentMonitorRect));
		//
		// }

		// Hack to simulate multi and single monitor setups...
		SnowSimulator.totalScreenRect.left = -1920;
		SnowSimulator.totalScreenRect.top = 0;
		SnowSimulator.totalScreenRect.right = 3840;
		SnowSimulator.totalScreenRect.bottom = 1050;

		for (int i = 0; i < 3; i++) {
			final WinDef.RECT currentMonitorRect = new WinDef.RECT();
			currentMonitorRect.left = 1920 * i - 1920;
			currentMonitorRect.right = 1920 * i;
			currentMonitorRect.top = 0;
			currentMonitorRect.bottom = 1050;

			SnowSimulator.getDesktops().add(new SnowWindow(currentMonitorRect));
		}

		for (final SnowWindow snowDesktop : SnowSimulator.getDesktops()) {
			System.out.println(snowDesktop.getScreenRect());
		}

		final SnowWindow[][] desktopsArray = new SnowWindow[9][9];

		desktopsArray[4][4] = SnowSimulator.desktops.get(0);

		for (final SnowWindow snowDesktop : SnowSimulator.getDesktops()) {

		}

		// Create an array for the windows
		SnowSimulator.setWindows(new int[SnowSimulator.totalScreenRect.right - SnowSimulator.totalScreenRect.left][SnowSimulator.totalScreenRect.bottom - SnowSimulator.totalScreenRect.top]); // 23MB

	}

	// Starts a thread which keeps track of all visible windows
	public void start() {

		final Thread windowUpdater = new Thread(new Runnable() {

			public void run() {
				try {
					SnowSimulator.this.getAllWindows();
				} catch (final InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while (true) {
					try {
						SnowSimulator.this.updateWindows();
					} catch (final InterruptedException e) {
						// TODO Auto-generated catch block
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
								if (x - SnowSimulator.totalScreenRect.left > 0 && x - SnowSimulator.totalScreenRect.left < SnowSimulator.getWindows().length && y > 0 && y < SnowSimulator.getWindows()[0].length - 2) {
									SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] + 1;
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
					if (x - SnowSimulator.totalScreenRect.left > 0 && x - SnowSimulator.totalScreenRect.left < SnowSimulator.getWindows().length && y > 0 && y < SnowSimulator.getWindows()[0].length - 2) {
						SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] - 1;
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

			if (rect.left != curRect.left || rect.right != curRect.right || rect.top != curRect.top || rect.bottom != curRect.bottom) {
				toUpdate.add(curWindow);
			}
		}

		for (final WindowHandle curWindow : toUpdate) {
			final WinDef.RECT newRect = new WinDef.RECT();

			User32.GetWindowRect(curWindow.getWindowHandle(), newRect);
			for (int x = newRect.left; x < newRect.right; x++) {
				for (int y = newRect.top; y < newRect.bottom; y++) {
					if (x - SnowSimulator.totalScreenRect.left >= 0 && x - SnowSimulator.totalScreenRect.left < SnowSimulator.getWindows().length && y >= 0 && y < SnowSimulator.getWindows()[0].length) {
						SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] + 1;
					}
				}
			}

			final WinDef.RECT rect = curWindow.getRect();
			for (int x = rect.left; x < rect.right; x++) {
				for (int y = rect.top; y < rect.bottom; y++) {
					if (x - SnowSimulator.totalScreenRect.left >= 0 && x - SnowSimulator.totalScreenRect.left < SnowSimulator.getWindows().length && y >= 0 && y < SnowSimulator.getWindows()[0].length) {
						SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] - 1;
					}
				}
			}

			SnowSimulator.this.windowList.get(SnowSimulator.this.windowList.indexOf(curWindow)).setRect(newRect);
		}

		this.getAllWindows();

		final int currentCursorPosX = MouseInfo.getPointerInfo().getLocation().x;
		final int currentCursorPosY = MouseInfo.getPointerInfo().getLocation().y;

		// Check if the snow is falling on the cursor
		for (int x = currentCursorPosX; x < currentCursorPosX + 12; x++) {
			for (int y = currentCursorPosY; y < currentCursorPosY + 16; y++) {
				if (x - SnowSimulator.totalScreenRect.left < SnowSimulator.getWindows().length && y < SnowSimulator.getWindows()[0].length) {
					SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] + 1;
				}
			}
		}

		for (int x = this.cursorRect.left; x < this.cursorRect.right; x++) {
			for (int y = this.cursorRect.top; y < this.cursorRect.bottom; y++) {
				if (x < SnowSimulator.getWindows().length && y < SnowSimulator.getWindows()[0].length) {
					SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] = SnowSimulator.getWindows()[x - SnowSimulator.totalScreenRect.left][y] - 1;
				}
			}
		}

		this.cursorRect.left = currentCursorPosX;
		this.cursorRect.right = currentCursorPosX + 12;
		this.cursorRect.top = currentCursorPosY;
		this.cursorRect.bottom = currentCursorPosY + 16;

		final int value = (int) (16 - (System.currentTimeMillis() - start));
		if (value > 0) {
			Thread.sleep(value);
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

}
