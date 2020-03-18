package com.bss.orange.ghost;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Singleton {
	private static Singleton single_instance = null; 
	public Properties prop;
	private Singleton() {
		try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
			prop = new Properties();
			prop.load(input);
		}catch (IOException io) {
			io.printStackTrace();
		}
	}
	public static Singleton getInstance() {
		if (single_instance == null) single_instance = new Singleton(); 
		return single_instance;
	}
}
