package com.horowitz.mickey;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {
	private static final String	SETTINGS_FILENAME	= "mickey.properties";

	Properties	                _properties;

	public Settings() {
		super();
		_properties = new Properties();
	}

	public void loadSettings() {

		try {
			File file = new File(SETTINGS_FILENAME);
			if (file.exists()) {
				FileInputStream fis = new FileInputStream(file);
				_properties.load(fis);
			} else {
				System.err.println("Settings file " + SETTINGS_FILENAME + " does not exist! Setting defaults");
				setDefaults();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setDefaults() {

		_properties.setProperty("refresh", "true");
		_properties.setProperty("mandatoryRefresh.time", "210");// 3.5h

		_properties.setProperty("zoom", "2");

		_properties.setProperty("railsHome", "5");
		_properties.setProperty("railsHome1", "100, 105, 114, 119, 124");
		_properties.setProperty("railsHome2", "100, 105, 111, 117, 130");
		_properties.setProperty("railsHome3", "100, 107, 114, 121, 135");
		_properties.setProperty("railsHome4", "100, 108, 116, 124, 143");

		_properties.setProperty("railsOut", "6");
		_properties.setProperty("railsOut1", "100, 104, 108, 112, 116, 117");// maglev rail reduced with 3px
		_properties.setProperty("railsOut2", "100, 107, 114, 122, 129, 138, 148");//back with 3px here
		_properties.setProperty("railsOut3", "100, 110, 119, 129, 138, 145");
		_properties.setProperty("railsOut4", "100, 112, 124, 136, 148, 157");


		_properties.setProperty("railYOffset1", "3");
		_properties.setProperty("railYOffset2", "4");
		_properties.setProperty("railYOffset3", "7");
		_properties.setProperty("railYOffset4", "9");
		_properties.setProperty("xOffset1", "20");
		_properties.setProperty("xOffset2", "22");
		_properties.setProperty("xOffset3", "26");
		_properties.setProperty("xOffset4", "32");

		// dangerous zones
		_properties.setProperty("zone1", "-497, -52, 74, 74");
		_properties.setProperty("zone2", "-277, -52, 74, 74");
		_properties.setProperty("zone3", "-57, -52, 74, 74");
    _properties.setProperty("zone1b", "-522, -118, 74, 74");
    _properties.setProperty("zone2b", "-292, -118, 74, 74");
    _properties.setProperty("zone3b", "-62, -118, 74, 74");
		_properties.setProperty("zone1a", "-390, -56, 74, 74");
		_properties.setProperty("zone2a", "-170, -56, 74, 74");
		
		_properties.setProperty("street1Y", "170");
		
		_properties.setProperty("ping", "true");
		_properties.setProperty("ping.time", "5");
	}

	public int getInt(String key) {
		String val = _properties.getProperty(key, "0");
		return Integer.parseInt(val.trim());
	}

	public int[] getArray(String key) {
		String val = _properties.getProperty(key);
		String[] split = val.split(",");
		int[] res = new int[split.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = Integer.parseInt(split[i].trim());
		}
		return res;
	}

	public Rectangle getArea(String key, Pixel p) {
		if (p != null)
			return getArea(key, p.x, p.y);
		else
			return getArea(key, 0, 0);
	}

	public Rectangle getArea(String key, int baseX, int baseY) {
		String val = _properties.getProperty(key);
		String[] split = val.split(",");
		Rectangle res = new Rectangle(baseX + Integer.parseInt(split[0].trim()), baseY + Integer.parseInt(split[1].trim()), Integer.parseInt(split[2]
		        .trim()), Integer.parseInt(split[3].trim()));
		return res;
	}

	public String getProperty(String key) {
		return _properties.getProperty(key);
	}

	public boolean containsKey(Object key) {
		return _properties.containsKey(key);
	}

	public String getProperty(String key, String defaultValue) {
		return _properties.getProperty(key, defaultValue);
	}

	public void saveSettings() {
		try {
			FileOutputStream fos = new FileOutputStream(new File(SETTINGS_FILENAME));
			_properties.store(fos, "Settings of Mickey");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
