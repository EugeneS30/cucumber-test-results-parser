package com.github.eugene.config;

import java.io.IOException;
import java.util.Properties;

public class PropertiesContainer {

	public static final Properties properties = new Properties();

	static {
		try {
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
			
			for (String propertyName : properties.stringPropertyNames()) {
				String systemPropertyValue = System.getProperty(propertyName);

				if (systemPropertyValue != null) {
					properties.setProperty(propertyName, systemPropertyValue);
				}
			}
			
		} catch (IOException e) {
			throw new ExceptionInInitializerError("Loading config file failed" + "\n" + e);
		}
	}
	
	 public static String getProperty(String key) {
	        return properties.getProperty(key);
	    }

//	private PropertiesContainer() throws IOException {
//
//		InputStream inputStream = null;
//
//		try {
//			String fileName = "config.properties";
//
//			inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
//
//			if (inputStream != null) {
//				properties.load(inputStream);
//			} else {
//				throw new FileNotFoundException("property file '" + fileName + "' not found in the classpath");
//			}
//
//			for (String propertyName : properties.stringPropertyNames()) {
//				String systemPropertyValue = System.getProperty(propertyName);
//
//				if (systemPropertyValue != null) {
//					properties.setProperty(propertyName, systemPropertyValue);
//				}
//			}
//
//		} catch (Exception e) {
//			System.out.println("Exception: " + e);
//		} finally {
//			inputStream.close();
//		}
//
//	}

//	public static synchronized PropertiesContainer getInstance() throws IOException {
//
//		if (instance == null) {
//			instance = new PropertiesContainer();
//		}
//
//		return instance;
//
//	}

//	public static Properties getPropValues() throws IOException {
//
//		if (properties == null) {
//			properties = new Properties();
//		}
//
//		return properties;
//
//	}
}
