package at.ac.uibk.chaas.minesteg.steg;

import at.ac.uibk.chaas.minesteg.StegoSystem;
import at.ac.uibk.chaas.minesteg.analyze.AllPacketDumper;
import at.ac.uibk.chaas.minesteg.analyze.StegoPacketDumper;
import net.minecraft.network.IPacket;

/**
 * Main entry point for the Minecraft network manager.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class PacketHandler {
    private static AllPacketDumper allPacketDumper = new AllPacketDumper();
    private static StegoPacketDumper stegoPacketDumper = new StegoPacketDumper();

    /**
     * Called for every send packet.
     *
     * @param packetIn the send packet.
     */
    // update packet in place!
    public static void send(IPacket<?> packetIn) {
        if (StegoSystem.getInstance().isSender() && "true".equalsIgnoreCase(StegoSystem.getInstance().getConfigVar("minesteg.enabled"))) {
            StegoSystem.getInstance().getEncoder().processPacket(packetIn);
        }

        if ("true".equalsIgnoreCase(StegoSystem.getInstance().getConfigVar("minesteg.analyze.logsteg.send"))) {
            stegoPacketDumper.logSendPacket(packetIn);
        }

        if ("true".equalsIgnoreCase(StegoSystem.getInstance().getConfigVar("minesteg.analyze.logall.send"))) {
            allPacketDumper.logSendPacket(packetIn);
        }
    }

    /**
     * Called for every received packet.
     *
     * @param packetIn the received packet.
     */
    public static void receive(IPacket<?> packetIn) {
        if (!StegoSystem.getInstance().isSender() && "true".equalsIgnoreCase(StegoSystem.getInstance().getConfigVar("minesteg.enabled"))) {
            StegoSystem.getInstance().getDecoder().processPacket(packetIn);
        }

        if ("true".equalsIgnoreCase(StegoSystem.getInstance().getConfigVar("minesteg.analyze.logsteg.receive"))) {
            stegoPacketDumper.logReceivedPacket(packetIn);
        }

        if ("true".equalsIgnoreCase(StegoSystem.getInstance().getConfigVar("minesteg.analyze.logall.receive"))) {
            allPacketDumper.logReceivedPacket(packetIn);
        }
    }
}
