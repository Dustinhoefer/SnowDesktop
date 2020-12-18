package data;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

//This represents a visible windows window
public class Window {

	private final HWND windowHandle;
	private final String title;
	private WinDef.RECT rect;
	private final int windowID;
	private boolean stillExists;

	public int getWindowID() {
		return this.windowID;
	}

	private static int windowCount = 0;

	public Window(final HWND windowHandle, final String title, final RECT rect) {
		this.windowHandle = windowHandle;
		this.title = title;
		this.rect = rect;
		this.windowID = Window.windowCount;
		Window.windowCount++;
		this.setStillExists(true);
	}

	public HWND getWindowHandle() {
		return this.windowHandle;
	}

	public String getTitle() {
		return this.title;
	}

	public WinDef.RECT getRect() {
		return this.rect;
	}

	public void setRect(final WinDef.RECT newRect) {
		this.rect = newRect;
	}

	public void setStillExists(final boolean stillExists) {
		this.stillExists = stillExists;
	}

	public boolean stillExists() {
		return this.stillExists;
	}
}
