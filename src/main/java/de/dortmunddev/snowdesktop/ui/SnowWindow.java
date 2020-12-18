package de.dortmunddev.snowdesktop.ui;

import java.awt.Color;

import javax.swing.JWindow;

import com.sun.jna.platform.win32.WinDef.RECT;

public class SnowWindow extends JWindow {

	private static final long serialVersionUID = 1L;
	private final RECT screenRect;
	private final SnowflakePanel snowflakePanel;

	public SnowWindow(final RECT currentScreenRect) {
		this.setSize(currentScreenRect.right - currentScreenRect.left,
				currentScreenRect.bottom - currentScreenRect.top);
		this.setLocation(currentScreenRect.left, currentScreenRect.top);
		this.setBackground(new Color(0, 0, 0, 0));
		this.setVisible(true);
		this.toBack();

		this.snowflakePanel = new SnowflakePanel(currentScreenRect);
		this.snowflakePanel.setDebug(false);
		this.add(this.snowflakePanel);
		this.screenRect = currentScreenRect;
	}

	public RECT getScreenRect() {
		return this.screenRect;
	}

	public SnowflakePanel getSnowflakePanel() {
		return this.snowflakePanel;
	}

}