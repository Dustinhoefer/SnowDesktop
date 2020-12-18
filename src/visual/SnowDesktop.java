package visual;

import java.awt.Color;

import javax.swing.JWindow;

import com.sun.jna.platform.win32.WinDef.RECT;

public class SnowDesktop extends JWindow {

	private static final long serialVersionUID = 1L;
	private static String version = "0.0.3";
	private final RECT screenRect;
	private final SnowflakePanel snowflakePanel;

	public SnowDesktop(final RECT currentScreenRect) {
		this.setSize(currentScreenRect.right - currentScreenRect.left, currentScreenRect.bottom - currentScreenRect.top);
		this.setLocation(currentScreenRect.left, currentScreenRect.top);
		this.setBackground(new Color(0, 0, 0, 0));
		this.setVisible(true);
		this.toBack();

		this.snowflakePanel = new SnowflakePanel(currentScreenRect);
		this.snowflakePanel.setDebug(false);
		this.add(this.snowflakePanel);
		this.screenRect = currentScreenRect;
	}

	public static String getVersion() {
		return SnowDesktop.version;
	}

	public static void setVersion(final String version) {
		SnowDesktop.version = version;
	}

	public RECT getScreenRect() {
		return this.screenRect;
	}

	public SnowflakePanel getSnowflakePanel() {
		// TODO Auto-generated method stub
		return this.snowflakePanel;
	}

}