package at.ac.uibk.chaas.minesteg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * A helper class for property files.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class PropertiesHelper {
    private static String fileSuffix = "";

    /**
     * @return the set file suffix.
     */
    public static String getFileSuffix() {
        return fileSuffix;
    }

    /**
     * Set a global file suffix for the settings file.
     *
     * @param fileSuffix the suffix.
     */
    public static void setFileSuffix(String fileSuffix) {
        PropertiesHelper.fileSuffix = fileSuffix;
    }

    /**
     * Get a URLConnection to the properties file.
     *
     * @return the URLConnection or null if connection failed.
     */
    public static URLConnection getPropertyFileConnection(String propertiesFile) {
        String propFileNoExt = propertiesFile.substring(0, propertiesFile.lastIndexOf(".properties"));

        // see https://stackoverflow.com/questions/3121449/getclass-getclassloader-getresourceasstream-is-caching-the-resource
        URL propRes = PropertiesHelper.class.getClassLoader().getResource(propFileNoExt + fileSuffix + ".properties");

        if (propRes != null) {
            try {
                URLConnection resConn = propRes.openConnection();
                resConn.setUseCaches(false);

                return resConn;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Read the properties file.
     *
     * @param propertiesFile the properties file.
     * @return the loaded properties.
     */
    public static Properties loadProperties(String propertiesFile) {
        System.out.println("Loading minesteg properties from file: " + propertiesFile + ", file suffix: " + fileSuffix);
        System.out.println("Current Working Directory: " + System.getProperty("user.dir"));

        URLConnection resConn = PropertiesHelper.getPropertyFileConnection(propertiesFile);
        InputStream propsStream = null;
        Properties props = new Properties();

        if (resConn != null) {
            try {
                propsStream = resConn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (propsStream != null) {
            try {
                props.load(propsStream);
            } catch (IOException e) {
                System.err.println("Failed to open properties file! " + e.getMessage());
            }

            try {
                propsStream.close();
            } catch (IOException e) {
                // ignore
            }
        }

        return props;
    }
}
