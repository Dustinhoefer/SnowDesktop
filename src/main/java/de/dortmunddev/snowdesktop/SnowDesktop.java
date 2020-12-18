//package snowdesktop;
//
//import java.awt.Dimension;
//import java.awt.Toolkit;
//
//public class SnowDesktopGUI {
//
//	/**
//	 * @param args
//	 */
//	public static void main(final String[] args) {
//		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		final SnowSimulator snowSimulator = new SnowSimulator(screenSize.width);
//		snowSimulator.start();
//	}
//}
package de.dortmunddev.snowdesktop;

import de.dortmunddev.snowdesktop.logic.SnowSimulator;
import de.dortmunddev.snowdesktop.ui.CustomTrayIcon;

public class SnowDesktop {

	public static void main(final String[] args) throws Exception {

		final CustomTrayIcon trayIconCustom = new CustomTrayIcon();
		trayIconCustom.setTrayIcon();

		final SnowSimulator snowSimulator = new SnowSimulator();
		snowSimulator.start();

	}

}