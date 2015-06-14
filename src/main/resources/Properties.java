public class Properties {
	
	Properties prop = new Properties();
	OutputStream output = null;
 
	try {
 
		output = new FileOutputStream("config.properties");
 
		// set the properties value
		prop.setProperty("database", "localhost");
		prop.setProperty("dbuser", "mkyong");
		prop.setProperty("dbpassword", "password");
 
		// save properties to project root folder
		prop.store(output, null);
 
	} catch (IOException io) {
		io.printStackTrace();
	} finally {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
 
	}

}
