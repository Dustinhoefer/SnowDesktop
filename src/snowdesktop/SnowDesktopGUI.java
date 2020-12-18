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
package snowdesktop;

import data.SnowSimulator;

public class SnowDesktopGUI {

	public static void main(final String[] args) throws Exception {

		final TrayIconCustom trayIconCustom = new TrayIconCustom();
		trayIconCustom.setTrayIcon();

		final SnowSimulator snowSimulator = new SnowSimulator();
		snowSimulator.start();

	}

}