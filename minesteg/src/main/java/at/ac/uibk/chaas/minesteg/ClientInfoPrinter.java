package at.ac.uibk.chaas.minesteg;

import java.util.List;
import java.util.Properties;

/**
 * A class that is used to print some information about the minecraft client in the game.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class ClientInfoPrinter {
    private static final String PROPERTIES_FILE = "minesteg.properties";
    private static final Properties settings = PropertiesHelper.loadProperties(PROPERTIES_FILE);

    /**
     * Press F3 while in game to see the output of this method.
     *
     * @param list the text-lines that are printed to the game debug screen.
     */
    public static void printClientInfo(List<String> list) {
        boolean enabled = "true".equalsIgnoreCase(settings.getProperty("minesteg.enabled", "false"));

        String mode;
        if (enabled) {
            mode = settings.getProperty("minesteg.mode", "UNKNOWN") + ",E";
        } else {
            mode = settings.getProperty("minesteg.mode", "UNKNOWN") + ",D";
        }
        list.add(0, "Minesteg modified client! Mode: " + mode);
    }
}
