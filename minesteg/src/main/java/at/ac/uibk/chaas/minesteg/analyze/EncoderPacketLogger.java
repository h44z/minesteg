package at.ac.uibk.chaas.minesteg.analyze;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import at.ac.uibk.chaas.minesteg.PropertiesHelper;
import net.minecraft.network.play.client.CPlayerPacket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * A class that is used to dump encoder packets to file.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class EncoderPacketLogger {
    private static final String CSV_SEPARATOR = ",";
    private static final int FLUSH_INTERVAL = 50;
    private BufferedWriter writer;
    private int flushCounter = 0;
    private long initialTime = 0;

    /**
     * Constructor. Sets up all file handles.
     */
    public EncoderPacketLogger() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        LocalDateTime now = LocalDateTime.now();
        String dateTimeStr = dtf.format(now);

        String logFilename = "minesteg_enc_log." + dateTimeStr + ".csv";

        try {
            Properties settings = PropertiesHelper.loadProperties("minesteg.properties");
            if ("true".equalsIgnoreCase(settings.getProperty("minesteg.analyze.logenc", "false"))) {
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
                "pitchOrig" +
                CSV_SEPARATOR +
                "yawOrig" +
                CSV_SEPARATOR +
                "pitchMod" +
                CSV_SEPARATOR +
                "yawMod" +
                CSV_SEPARATOR +
                "pitchOrigWire" +
                CSV_SEPARATOR +
                "yawOrigWire" +
                CSV_SEPARATOR +
                "pitchModWire" +
                CSV_SEPARATOR +
                "yawModWire" +
                CSV_SEPARATOR +
                "bitPitch" +
                CSV_SEPARATOR +
                "bitYaw" +
                CSV_SEPARATOR +
                "sendCounter" +
                CSV_SEPARATOR +
                "bytePosition" +
                CSV_SEPARATOR +
                "byteAsBinary" +
                "\n";
        try {
            writer.write(csvLine);
        } catch (IOException e) {
            System.out.println("Failed to write encoder logfile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Log the encoder packet to file.
     *
     * @param playerPacketOrig     the original, unmodified packet.
     * @param playerPacketModified the modified packet with stegodata.
     * @param type                 the type of the packet.
     * @param bitPitch             the LSB of pitch value.
     * @param bitYaw               the LSB of the yaw value.
     * @param sendCounter          the send counter value of the encoder.
     * @param bytePosition         the current byte position of the encoder.
     * @param currentByte          the current byte value processed by the encoder.
     */
    public void logPacket(CPlayerPacket playerPacketOrig, CPlayerPacket playerPacketModified, String type, byte bitPitch, byte bitYaw,
                          int sendCounter, int bytePosition, Byte currentByte) {
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
                playerPacketOrig.getPitch(0) +
                CSV_SEPARATOR +
                playerPacketOrig.getYaw(0) +
                CSV_SEPARATOR +
                playerPacketModified.getPitch(0) +
                CSV_SEPARATOR +
                playerPacketModified.getYaw(0) +
                CSV_SEPARATOR +
                HelperUtil.getWireValue(playerPacketOrig.getPitch(0)) +
                CSV_SEPARATOR +
                HelperUtil.getWireValue(playerPacketOrig.getYaw(0)) +
                CSV_SEPARATOR +
                HelperUtil.getWireValue(playerPacketModified.getPitch(0)) +
                CSV_SEPARATOR +
                HelperUtil.getWireValue(playerPacketModified.getYaw(0)) +
                CSV_SEPARATOR +
                bitPitch +
                CSV_SEPARATOR +
                bitYaw +
                CSV_SEPARATOR +
                sendCounter +
                CSV_SEPARATOR +
                bytePosition +
                CSV_SEPARATOR +
                HelperUtil.byteToString(currentByte) +
                "\n";

        try {
            writer.write(csvLine);
        } catch (IOException e) {
            System.out.println("Failed to write encoder logfile: " + e.getMessage());
            e.printStackTrace();
        }

        flushCounter++;

        if (flushCounter == FLUSH_INTERVAL) {
            try {
                writer.flush();
            } catch (IOException e) {
                System.out.println("Failed to flush encoder logfile: " + e.getMessage());
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
