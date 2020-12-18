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

public class SnowDesktop {

	private static String version = "0.0.3";

	public static void main(final String[] args) throws Exception {
		final SnowSimulator snowSimulator = new SnowSimulator();
		snowSimulator.start();
	}

	public static String getVersion() {
		return version;
	}

	public static void setVersion(final String version) {
		SnowDesktop.version = version;
	}

}