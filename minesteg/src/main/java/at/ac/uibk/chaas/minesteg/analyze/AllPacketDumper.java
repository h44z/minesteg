package at.ac.uibk.chaas.minesteg.analyze;

import at.ac.uibk.chaas.minesteg.PropertiesHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * A class that is used to dump Minecraft network packets to file.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class AllPacketDumper {
    private static final String CSV_SEPARATOR = ",";
    private static final int FLUSH_INTERVAL = 500;
    private int flushCounterOutgoing = 0;
    private int flushCounterIncoming = 0;
    private BufferedWriter incomingWriter = null;
    private BufferedWriter outgoingWriter = null;
    private int playerId = 0;
    private long initialTime = 0;

    /**
     * Constructor. Sets up all file handles.
     */
    public AllPacketDumper() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        LocalDateTime now = LocalDateTime.now();
        String dateTimeStr = dtf.format(now);

        String incomingFilename = "packets.in." + dateTimeStr + ".csv";
        String outgoingFilename = "packets.out." + dateTimeStr + ".csv";

        try {
            Properties settings = PropertiesHelper.loadProperties("minesteg.properties");
            if ("true".equalsIgnoreCase(settings.getProperty("minesteg.analyze.logall.send", "false"))) {
                outgoingWriter = new BufferedWriter(new FileWriter(outgoingFilename));
            }
            if ("true".equalsIgnoreCase(settings.getProperty("minesteg.analyze.logall.receive", "false"))) {
                incomingWriter = new BufferedWriter(new FileWriter(incomingFilename));
            }
        } catch (IOException e) {
            System.err.println("Got an exception while initializing file writers: " + e.getMessage());
            System.err.println(e.getMessage());
        }

        writeReceiveHeader();
        writeSendHeader();
    }

    /**
     * Write the CSV header for the outgoing packet logfile.
     */
    private void writeSendHeader() {
        if (outgoingWriter == null) {
            return;
        }

        String csvLine = "time" +
                CSV_SEPARATOR +
                "reltime" +
                CSV_SEPARATOR +
                "packetClass" +
                CSV_SEPARATOR +
                "entitytype" +
                CSV_SEPARATOR +
                "entityid" +
                CSV_SEPARATOR +
                "mapinit" +
                "\n";
        try {
            outgoingWriter.write(csvLine);
        } catch (IOException e) {
            System.out.println("Failed to write sender logfile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Write the CSV header for the incoming packet logfile.
     */
    private void writeReceiveHeader() {
        if (incomingWriter == null) {
            return;
        }

        String csvLine = "time" +
                CSV_SEPARATOR +
                "reltime" +
                CSV_SEPARATOR +
                "packetClass" +
                CSV_SEPARATOR +
                "entitytype" +
                CSV_SEPARATOR +
                "entityid" +
                CSV_SEPARATOR +
                "mapinit" +
                "\n";
        try {
            incomingWriter.write(csvLine);
        } catch (IOException e) {
            System.out.println("Failed to write receiver logfile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Store the user ID of the sender.
     */
    private void setSenderId() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.world != null) {
            if (minecraft.player != null) {
                this.playerId = minecraft.player.getEntityId();
            }
        }
    }

    /**
     * Log a send (outgoing) packet to the CSV file.
     *
     * @param packetOut the send packet.
     */
    public void logSendPacket(IPacket<?> packetOut) {
        if (outgoingWriter == null) {
            return;
        }

        if (playerId == 0) {
            setSenderId();
        }

        if (initialTime == 0) {
            initialTime = System.currentTimeMillis();
        }

        int entityID = playerId;
        String entityType = "player-self";
        String packetType = packetOut.getClass().getName();
        boolean mapInitialized = Minecraft.getInstance().world != null && Minecraft.getInstance().player != null;

        String csvLine = System.currentTimeMillis() +
                CSV_SEPARATOR +
                (System.currentTimeMillis() - initialTime) +
                CSV_SEPARATOR +
                packetType +
                CSV_SEPARATOR +
                entityType +
                CSV_SEPARATOR +
                entityID +
                CSV_SEPARATOR +
                mapInitialized +
                "\n";

        try {
            outgoingWriter.write(csvLine);
        } catch (IOException e) {
            System.out.println("Failed to write outgoing logfile: " + e.getMessage());
            e.printStackTrace();
        }

        flushCounterOutgoing++;

        if (flushCounterOutgoing == FLUSH_INTERVAL) {
            try {
                outgoingWriter.flush();
            } catch (IOException e) {
                System.out.println("Failed to flush outgoing log: " + e.getMessage());
                e.printStackTrace();
            }
            flushCounterOutgoing = 0;
        }
    }

    /**
     * Log a received (incoming) packet to the CSV file.
     *
     * @param packetIn the received packet.
     */
    public void logReceivedPacket(IPacket<?> packetIn) {
        if (incomingWriter == null) {
            return;
        }

        if (initialTime == 0) {
            initialTime = System.currentTimeMillis();
        }

        int entityID;
        String entityType;
        String packetType = packetIn.getClass().getName();
        boolean mapInitialized = Minecraft.getInstance().world != null && Minecraft.getInstance().player != null;

        // Received packets contain a entity id
        try {
            Field f = getDeclaredFieldInHierarchy(packetIn.getClass(), "entityId");

            if (f != null) {
                f.setAccessible(true);
                Integer entityIDTmp = (Integer) f.get(packetIn); //IllegalAccessException

                if (entityIDTmp != null) {
                    entityID = entityIDTmp;

                    // lookup entity type
                    entityType = "unknown";
                    if (Minecraft.getInstance().world != null) {
                        Entity entity = Minecraft.getInstance().world.getEntityByID(entityID);
                        if (entity != null) {
                            entityType = entity.getType().getName().getString();
                        }
                    }
                } else {
                    entityID = 0;
                    entityType = "entityidnull";
                }
            } else {
                entityID = 0;
                entityType = "noentityid";
            }
        } catch (IllegalAccessException e) {
            // ignore error
            entityID = 0;
            entityType = "exception";
        }

        String csvLine = System.currentTimeMillis() +
                CSV_SEPARATOR +
                (System.currentTimeMillis() - initialTime) +
                CSV_SEPARATOR +
                packetType +
                CSV_SEPARATOR +
                entityType +
                CSV_SEPARATOR +
                entityID +
                CSV_SEPARATOR +
                mapInitialized +
                "\n";

        try {
            incomingWriter.write(csvLine);
        } catch (IOException e) {
            System.out.println("Failed to write incoming logfile: " + e.getMessage());
            e.printStackTrace();
        }

        flushCounterIncoming++;

        if (flushCounterIncoming == FLUSH_INTERVAL) {
            try {
                incomingWriter.flush();
            } catch (IOException e) {
                System.out.println("Failed to flush incoming log: " + e.getMessage());
                e.printStackTrace();
            }
            flushCounterIncoming = 0;
        }
    }

    /**
     * Get the reflection field, also check parent classes.
     * TODO: replace with HelperUtil.findUnderlyingField
     *
     * @param clazz     the class that should be searched for the field.
     * @param fieldName the field name.
     * @return the reflection field or null if field was not found.
     */
    private Field getDeclaredFieldInHierarchy(Class<?> clazz, String fieldName) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return getDeclaredFieldInHierarchy(clazz.getSuperclass(), fieldName);
        }
    }
}
