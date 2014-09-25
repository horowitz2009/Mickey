/**
 * 
 */
package com.horowitz.mickey;

import java.awt.Dimension;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author zhristov
 * 
 */
public class Launcher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Map<String, String> argsMap = new Hashtable<String, String>(3);

		if (args.length > 0) {
			for (String s : args) {
				String[] split = s.trim().split("=");
				argsMap.put(split[0], split[1]);
			}
		}

		Boolean refresh = null;

		String sr = argsMap.get("refresh");
		if (sr != null) {
			refresh = Boolean.parseBoolean(sr);
		}

		Boolean ping = null;
		
		String sp = argsMap.get("ping");
		if (sp != null) {
		  ping = Boolean.parseBoolean(sp);
		}

		MainFrame frame = new MainFrame(refresh, ping);

		frame.pack();
		frame.setSize(new Dimension(frame.getSize().width + 2, frame.getSize().height + 2));

		frame.setLocationRelativeTo(null);

		frame.setVisible(true);


	}

}
