package at.ac.uibk.chaas.minesteg.steg;

/**
 * Some preferences for the stegosystem.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class SharedPreferences {
    public static final int MIN_REPEAT_COUNT = 1; // how often to resend the packet to the server
    public static final int SYNC_INTERVAL = (MIN_REPEAT_COUNT + 1) * 4 + 2; // after 1 byte send a sync packet
    public static final int HISTORY_LENGTH = 5; // used for trend calculation
    public static final long INIT_DELAY_SENDER_MS = 2000;   // initial delay before processing starts
    public static final long INIT_DELAY_RECEIVER_MS = 3000; // initial delay before processing starts
}
