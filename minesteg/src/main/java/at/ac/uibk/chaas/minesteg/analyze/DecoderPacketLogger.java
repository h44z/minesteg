package at.ac.uibk.chaas.minesteg.analyze;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import at.ac.uibk.chaas.minesteg.PropertiesHelper;
import net.minecraft.network.play.server.SEntityPacket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * A class that is used to dump decoder packets to file.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class DecoderPacketLogger {
    private static final String CSV_SEPARATOR = ",";
    private static final int FLUSH_INTERVAL = 50;
    private BufferedWriter writer = null;
    private int flushCounter = 0;
    private long initialTime = 0;

    /**
     * Constructor. Sets up all file handles.
     */
    public DecoderPacketLogger() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        LocalDateTime now = LocalDateTime.now();
        String dateTimeStr = dtf.format(now);

        String logFilename = "minesteg_dec_log." + dateTimeStr + ".csv";

        try {
            Properties settings = PropertiesHelper.loadProperties("minesteg.properties");
            if ("true".equalsIgnoreCase(settings.getProperty("minesteg.analyze.logdec", "false"))) {
                writer = new BufferedWriter(new FileWriter(logFilename));
            }
        } catch (IOException e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
        writeHeader();
    }

    /**
     * Write the CSV header for the packet logfile.
     */
    private void writeHeader() {
        if (writer == null) {
            return;
        }

        String csvLine = "time" +
                CSV_SEPARATOR +
                "reltime" +
                CSV_SEPARATOR +
                "type" +
                CSV_SEPARATOR +
                "pitch" +
                CSV_SEPARATOR +
                "yaw" +
                CSV_SEPARATOR +
                "bitPitch" +
                CSV_SEPARATOR +
                "bitYaw" +
                CSV_SEPARATOR +
                "bytePosition" +
                CSV_SEPARATOR +
                "byteAsBinary" +
                CSV_SEPARATOR +
                "bitrate" +
                "\n";
        try {
            writer.write(csvLine);
        } catch (IOException e) {
            System.out.println("Failed to write encoder logfile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Log a decoder packet to file.
     *
     * @param receivedPacket the received packet object.
     * @param type           the type of the packet.
     * @param bitPitch       the LSB of pitch value.
     * @param bitYaw         the LSB of the yaw value.
     * @param bytePosition   the current byte position of the decoder.
     * @param currentByte    the current byte value processed by the decoder.
     * @param bitrate        the current bit rate.
     */
    public void logPacket(SEntityPacket receivedPacket, String type, byte bitPitch, byte bitYaw, int bytePosition, Byte currentByte,
                          float bitrate) {
        if (writer == null) {
            return;
        }

        if (initialTime == 0) {
            initialTime = System.currentTimeMillis();
        }

        String csvLine = System.currentTimeMillis() +
                CSV_SEPARATOR +
                (System.currentTimeMillis() - initialTime) +
                CSV_SEPARATOR +
                type +
                CSV_SEPARATOR +
                receivedPacket.getPitch() +
                CSV_SEPARATOR +
                receivedPacket.getYaw() +
                CSV_SEPARATOR +
                bitPitch +
                CSV_SEPARATOR +
                bitYaw +
                CSV_SEPARATOR +
                bytePosition +
                CSV_SEPARATOR +
                HelperUtil.byteToString(currentByte) +
                CSV_SEPARATOR +
                bitrate +
                "\n";

        try {
            writer.write(csvLine);
        } catch (IOException e) {
            System.out.println("Failed to write decoder logfile: " + e.getMessage());
            e.printStackTrace();
        }

        flushCounter++;

        if (flushCounter == FLUSH_INTERVAL) {
            try {
                writer.flush();
            } catch (IOException e) {
                System.out.println("Failed to flush decoder logfile: " + e.getMessage());
                e.printStackTrace();
            }
            flushCounter = 0;
        }
    }

    /**
     * Close the log file handle.
     */
    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
