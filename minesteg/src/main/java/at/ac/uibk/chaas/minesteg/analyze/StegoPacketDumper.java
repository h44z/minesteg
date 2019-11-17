package at.ac.uibk.chaas.minesteg.analyze;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import at.ac.uibk.chaas.minesteg.PropertiesHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SEntityPacket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * A class that is used to dump Minecraft network packets to file. This dumper creates more detailed
 * log files than the {@link AllPacketDumper}.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class StegoPacketDumper {
    private static final String CSV_SEPARATOR = ",";
    private static final int FLUSH_INTERVAL = 100;
    private static final boolean PRINT_TO_STDOUT = false;
    private int flushCounterOutgoing = 0;
    private int flushCounterIncoming = 0;
    private final Properties settings = PropertiesHelper.loadProperties("minesteg.properties");
    private BufferedWriter incomingWriter = null;
    private BufferedWriter outgoingWriter = null;
    private int senderId = 0;
    private PlayerEntity senderEntity = null;
    private long initialTime = 0;

    /**
     * Constructor. Sets up all file handles.
     */
    public StegoPacketDumper() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        LocalDateTime now = LocalDateTime.now();
        String dateTimeStr = dtf.format(now);

        String incomingFilename = "stegopacketlog.in." + dateTimeStr + ".csv";
        String outgoingFilename = "stegopacketlog.out." + dateTimeStr + ".csv";

        try {
            Properties settings = PropertiesHelper.loadProperties("minesteg.properties");
            if ("true".equalsIgnoreCase(settings.getProperty("minesteg.analyze.logsteg.send", "false"))) {
                outgoingWriter = new BufferedWriter(new FileWriter(outgoingFilename));
            }
            if ("true".equalsIgnoreCase(settings.getProperty("minesteg.analyze.logsteg.receive", "false"))) {
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
                "pitchWire" +
                CSV_SEPARATOR +
                "yawWire" +
                CSV_SEPARATOR +
                "pitch" +
                CSV_SEPARATOR +
                "yaw" +
                CSV_SEPARATOR +
                "senderId" +
                CSV_SEPARATOR +
                "packetClass" +
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
                "pitch" +
                CSV_SEPARATOR +
                "yaw" +
                CSV_SEPARATOR +
                "senderId" +
                CSV_SEPARATOR +
                "packetClass" +
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
                this.senderId = minecraft.player.getEntityId();
            }
        }
    }

    /**
     * Store the entity-object of the sender.
     */
    private void setSenderEntity() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.world != null) {
            for (int i = 0; i < minecraft.world.getPlayers().size(); ++i) {
                PlayerEntity playerentity = minecraft.world.getPlayers().get(i);
                if (settings.get("minesteg.sender").equals(playerentity.getName().getString())) {
                    this.senderEntity = playerentity;
                    break;
                }
            }
        }
    }

    /**
     * Log a send (outgoing) packet to the CSV file.
     *
     * @param packetOut the send packet.
     */
    public void logSendPacket(IPacket<?> packetOut) {
        if (!(packetOut instanceof CPlayerPacket.RotationPacket) && !(packetOut instanceof CPlayerPacket.PositionRotationPacket)) {
            return; // ignore invalid packets
        }

        if (senderId == 0) {
            setSenderId();
        }

        if (initialTime == 0) {
            initialTime = System.currentTimeMillis();
        }

        CPlayerPacket packet = (CPlayerPacket) packetOut;

        if (PRINT_TO_STDOUT) {
            System.out.println(">>>  P=" + HelperUtil.getWireValue(packet.getPitch(0)) +
                    "\t Y=" + HelperUtil.getWireValue(packet.getYaw(0)) + "\t | \t\t Pf=" + packet.getPitch(0) + "\t Yf=" + packet.getYaw(0));
        }

        if (outgoingWriter != null) {
            String csvLine = System.currentTimeMillis() +
                    CSV_SEPARATOR +
                    (System.currentTimeMillis() - initialTime) +
                    CSV_SEPARATOR +
                    HelperUtil.getWireValue(packet.getPitch(0)) +
                    CSV_SEPARATOR +
                    HelperUtil.getWireValue(packet.getYaw(0)) +
                    CSV_SEPARATOR +
                    packet.getPitch(0) +
                    CSV_SEPARATOR +
                    packet.getYaw(0) +
                    CSV_SEPARATOR +
                    senderId +
                    CSV_SEPARATOR +
                    packetOut.getClass().getName() +
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
    }

    /**
     * Log a received (incoming) packet to the CSV file.
     *
     * @param packetIn the received packet.
     */
    public void logReceivedPacket(IPacket<?> packetIn) {
        if (!(packetIn instanceof SEntityPacket.MovePacket) && !(packetIn instanceof SEntityPacket.LookPacket)) {
            return; // ignore invalid packets
        }

        SEntityPacket packet = (SEntityPacket) packetIn;

        // Check if packet is from sender
        if (senderEntity == null) {
            setSenderEntity();
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean isFromSender = packet.getEntity(minecraft.world) != null && packet.getEntity(minecraft.world).equals(senderEntity);

        if (!isFromSender) {
            return; // skip packets from other senders
        }

        if (initialTime == 0) {
            initialTime = System.currentTimeMillis();
        }

        if (PRINT_TO_STDOUT) {
            System.out.println("<<<  P=" + packet.getPitch() + "\t Y=" + packet.getYaw());
        }

        if (incomingWriter != null) {
            String csvLine = System.currentTimeMillis() +
                    CSV_SEPARATOR +
                    (System.currentTimeMillis() - initialTime) +
                    CSV_SEPARATOR +
                    packet.getPitch() +
                    CSV_SEPARATOR +
                    packet.getYaw() +
                    CSV_SEPARATOR +
                    (senderEntity != null ? senderEntity.getEntityId() : 0) +
                    CSV_SEPARATOR +
                    packetIn.getClass().getName() +
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
    }
}
