package at.ac.uibk.chaas.minesteg;

import at.ac.uibk.chaas.minesteg.rmi.RMIServer;
import at.ac.uibk.chaas.minesteg.steg.Decoder;
import at.ac.uibk.chaas.minesteg.steg.Encoder;
import at.ac.uibk.chaas.minesteg.steg.MessageBuffer;

import java.util.Properties;

/**
 * The main class for the steganographic system.
 * This class handles the state of the message buffer, encoder and decoder.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class StegoSystem {
    private final Properties settings = PropertiesHelper.loadProperties("minesteg.properties");

    private final RMIServer rmiServer;
    private final MessageBuffer messageBuffer;
    private Encoder encoder;
    private Decoder decoder;

    private static final class InstanceHolder {
        static final StegoSystem INSTANCE = new StegoSystem();
    }

    /**
     * Constructor.
     */
    private StegoSystem() {
        messageBuffer = new MessageBuffer();
        rmiServer = new RMIServer(messageBuffer, Integer.parseInt(settings.getProperty("minesteg.rmiport")));
    }

    /**
     * Get a singleton instance of the stegosystem.
     *
     * @return the instance.
     */
    public static StegoSystem getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Initialize the system.
     * Start decoder or encoder, depending on the configuration.
     */
    public void startup() {
        System.out.println(">> STEGO SYSTEM STARTING!");
        System.out.println(">> ----------------------");
        System.out.println(">> RMI SERVER STARTING!");
        rmiServer.run();
        System.out.println(">> RMI SERVER STARTED!");
        System.out.println(">> STEGO MODE: " + settings.get("minesteg.mode"));

        if (isSender()) {
            encoder = new Encoder(messageBuffer, settings.getProperty("minesteg.receiver"), getStegoKey());
        } else {
            decoder = new Decoder(messageBuffer, settings.getProperty("minesteg.sender"), getStegoKey());
        }

        System.out.println(">> STEGO SYSTEM INITIALIZED!");
        System.out.println(">> -------------------------");
    }

    /**
     * @return the encoder instance.
     */
    public Encoder getEncoder() {
        return encoder;
    }

    /**
     * @return the decoder instance.
     */
    public Decoder getDecoder() {
        return decoder;
    }

    /**
     * @return if minesteg.mode is set to SENDER in the properties.
     */
    public boolean isSender() {
        return settings.get("minesteg.mode").equals("SENDER");
    }

    /**
     * Get the configuration value for the given needle.
     *
     * @param varName the needle.
     * @return the value.
     */
    public String getConfigVar(String varName) {
        return settings.getProperty(varName);
    }

    /**
     * @return the stego key that is set in minesteg.stegokey.
     */
    private int[] getStegoKey() {
        String[] stegoKeyStr = settings.getProperty("minesteg.stegokey").split(",");
        int[] stegoKey = new int[stegoKeyStr.length];

        for (int i = 0; i < stegoKeyStr.length; i++) {
            stegoKey[i] = Integer.parseInt(stegoKeyStr[i]);
        }

        return stegoKey;
    }
}
