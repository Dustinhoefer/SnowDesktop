package de.dortmunddev.snowdesktop.ui;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import de.dortmunddev.snowdesktop.SnowDesktop;
import de.dortmunddev.snowdesktop.logic.SnowSimulator;

public class CustomTrayIcon {
	static Image image = Toolkit.getDefaultToolkit().getImage(CustomTrayIcon.class.getResource("/snowflake_icon.png"));
	static TrayIcon trayIcon = new TrayIcon(CustomTrayIcon.image, "Snowflake V " + SnowDesktop.getVersion());

	PopupMenu popupMenu = new PopupMenu();

	public CustomTrayIcon() {

	}

	// Create a tray icon with the menus: Schneestärke and exit;
	// Schneestärke has the submenus: leicht, mittel and stark, which define the
	// amount of snow, that will fall
	// exit will exit the program
	public void setTrayIcon() {
		if (SystemTray.isSupported()) {
			final SystemTray tray = SystemTray.getSystemTray();
			CustomTrayIcon.trayIcon.setImageAutoSize(true);
			try {
				tray.add(CustomTrayIcon.trayIcon);
			} catch (final AWTException e) {
				System.err.println(e.getMessage());
			}

			final Menu snowIntensityMenu = new Menu("Schneestärke");
			final CheckboxMenuItem cbLow = new CheckboxMenuItem("Leicht");
			final CheckboxMenuItem cbMid = new CheckboxMenuItem("Mittel");
			final CheckboxMenuItem cbHigh = new CheckboxMenuItem("Stark");
			final CheckboxMenuItem cbOff = new CheckboxMenuItem("Aus");
			cbOff.setState(true);

			cbLow.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					cbMid.setState(false);
					cbHigh.setState(false);
					cbOff.setState(false);
					SnowSimulator.setSnowIntensity(1);
				}
			});

			cbMid.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					cbLow.setState(false);
					cbHigh.setState(false);
					cbOff.setState(false);
					SnowSimulator.setSnowIntensity(3);
				}
			});

			cbHigh.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					cbLow.setState(false);
					cbMid.setState(false);
					cbOff.setState(false);
					SnowSimulator.setSnowIntensity(20);
				}
			});

			cbOff.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					cbLow.setState(false);
					cbMid.setState(false);
					cbHigh.setState(false);
					SnowSimulator.setSnowIntensity(0);
				}
			});

			final MenuItem exitItem = new MenuItem("Exit");
			exitItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					System.exit(0);
				}
			});

			snowIntensityMenu.add(cbLow);
			snowIntensityMenu.add(cbMid);
			snowIntensityMenu.add(cbHigh);
			snowIntensityMenu.add(cbOff);

			// Add components to pop-up menu
			this.popupMenu.add(snowIntensityMenu);
			this.popupMenu.addSeparator();
			this.popupMenu.add(exitItem);

			SnowSimulator.setSnowIntensity(20);

			CustomTrayIcon.trayIcon.setPopupMenu(this.popupMenu);
		}
	}

	public void showInfoMsg(final String msg, final String title) {
		CustomTrayIcon.trayIcon.displayMessage(title, msg, TrayIcon.MessageType.INFO);
	}

	public void showErrorMsg(final String msg, final String title) {
		CustomTrayIcon.trayIcon.displayMessage(title, msg, TrayIcon.MessageType.ERROR);
	}

	public void showWarningMsg(final String msg, final String title) {
		CustomTrayIcon.trayIcon.displayMessage(title, msg, TrayIcon.MessageType.WARNING);
	}

	public void showMsg(final String msg, final String title) {
		CustomTrayIcon.trayIcon.displayMessage(title, msg, TrayIcon.MessageType.NONE);
	}
}
