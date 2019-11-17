package at.ac.uibk.chaas.minesteg.steg;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import at.ac.uibk.chaas.minesteg.analyze.DecoderPacketLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SEntityPacket;

import static at.ac.uibk.chaas.minesteg.steg.SharedPreferences.INIT_DELAY_RECEIVER_MS;

/**
 * The decoder implementation for the steganographic system.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class Decoder {
    private static final boolean PRINT_TO_STDOUT = true;
    private static final long TIME_DIFF_BITRATE_MS = 5000;

    private final MessageBuffer messageBuffer;
    private final String senderName;
    private final int[] stegoKey;
    private final DecoderPacketLogger logger;

    private Byte currentByte = 0;
    private int currentBytePosition = 0;
    private byte previousPitch = 0;
    private byte previousYaw = 0;
    private boolean initialized = false;
    private long firstConnMs = 0;

    // Bitrate calucation
    private long bitCounter = 0;
    private long timeCounterMs = 0;
    private long prevBitTimeMs = 0;
    private float bitrate = 0;

    /**
     * Constructor.
     *
     * @param messageBuffer the message buffer instance. This buffer contains the decoded secret message.
     * @param senderName    the name of the embedding player.
     * @param stegoKey      the stegokey.
     */
    public Decoder(MessageBuffer messageBuffer, String senderName, int[] stegoKey) {
        this.messageBuffer = messageBuffer;
        this.senderName = senderName;
        this.stegoKey = stegoKey;
        logger = new DecoderPacketLogger();
    }

    /**
     * @return true if initialization of the decoder is completed.
     */
    private boolean checkInitialized() {
        if (initialized) {
            return true;
        }

        if (firstConnMs == 0) {
            firstConnMs = System.currentTimeMillis();
            return false;
        }

        long currTimeMs = System.currentTimeMillis();
        if (currTimeMs > firstConnMs + INIT_DELAY_RECEIVER_MS) {
            initialized = true;
            return true;
        }

        return false;
    }

    /**
     * Check if the given packet is valid for the decoder.
     *
     * @param packet the packet object.
     * @return true if packet can be processed by the decoder.
     */
    private boolean isValidPacket(IPacket<?> packet) {
        return packet instanceof SEntityPacket.MovePacket || packet instanceof SEntityPacket.LookPacket;
    }

    /**
     * Check if the given packet was sent by the embedding player.
     *
     * @param packet the packet object.
     * @return true if the packet was sent by the embedding player.
     */
    private boolean isPacketFromSender(SEntityPacket packet) {
        PlayerEntity sender = null;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.world != null) {
            for (int i = 0; i < minecraft.world.getPlayers().size(); ++i) {
                PlayerEntity playerentity = minecraft.world.getPlayers().get(i);
                if (senderName.equals(playerentity.getName().getString())) {
                    sender = playerentity;
                    break;
                }
            }

            // getEntity can still return null, documentation/annotation is incorrect!
            return packet.getEntity(minecraft.world) != null && packet.getEntity(minecraft.world).equals(sender);
        }

        return false;
    }

    /**
     * Main entry point for the decoder. This method is called for every received packet.
     *
     * @param packet the incoming packet.
     */
    public void processPacket(IPacket<?> packet) {
        if (!HelperUtil.isPlayerVisible(senderName)) {
            currentByte = 0;
            currentBytePosition = 0;
            previousPitch = 0;
            previousYaw = 0;
            initialized = false;
            firstConnMs = 0;

            return; // cannot process packet
        }

        if (!checkInitialized()) {
            return;
        }

        if (!isValidPacket(packet) || !isPacketFromSender((SEntityPacket) packet)) {
            return; // cannot process packet
        }

        if (PRINT_TO_STDOUT) {
            System.out.println("-------------------------- Packet:");
        }
        String packetType;

        byte pitch = ((SEntityPacket) packet).getPitch();
        byte yaw = ((SEntityPacket) packet).getYaw();

        // synchronization package received
        if (stegoKeyContains(pitch) || stegoKeyContains(yaw)) {
            packetType = "sync";
            if (messageBuffer.size() == 0) {
                packetType = "sync_initial";
            }

            if (PRINT_TO_STDOUT) {
                System.out.println("Sync Package: P = " + pitch + " Y = " + yaw);
            }

            // add incomplete byte to show transmission error
            if (currentBytePosition != 0) {
                System.out.println("-------------------------- Failure:");
                System.out.println(">>>>>> Failed: char=" + (char) currentByte.byteValue() + " byte=" + currentByte +
                        " binary=" + HelperUtil.byteToString(currentByte));
                messageBuffer.push((byte) 0xff);
            }

            currentByte = 0;
            currentBytePosition = 0;
            previousPitch = pitch;
            previousYaw = yaw;
        } else {
            packetType = "valid";
            byte bitPitch = extractBit(pitch);
            byte bitYaw = extractBit(yaw);

            if (PRINT_TO_STDOUT) {
                System.out.println("Valid Package: P = " + pitch + " Y = " + yaw + " (" + bitPitch + "|" + bitYaw + ") pos=" + currentBytePosition);
            }
            if (previousPitch != pitch || previousYaw != yaw) {
                packetType = "valid_bit"; // used to calculate bitrate afterwards
                previousPitch = pitch;
                currentByte = setBit(currentBytePosition++, currentByte, bitPitch);

                previousYaw = yaw;
                currentByte = setBit(currentBytePosition++, currentByte, bitYaw);

                // calculate bitrate
                long currentTimeMs = System.currentTimeMillis();
                if (prevBitTimeMs == 0) {
                    prevBitTimeMs = currentTimeMs;
                }

                bitCounter += 2;
                timeCounterMs += (currentTimeMs - prevBitTimeMs);
                prevBitTimeMs = currentTimeMs;

                if (timeCounterMs >= TIME_DIFF_BITRATE_MS) {
                    bitrate = bitCounter / (float) timeCounterMs * 1000;
                    if (PRINT_TO_STDOUT) {
                        System.out.println("-------------------------- Bitrate: " + bitrate + " bit/s");
                    }
                    bitCounter = 0;
                    timeCounterMs = 0;
                }
            }
        }

        logger.logPacket((SEntityPacket) packet, packetType, extractBit(pitch), extractBit(yaw), currentBytePosition, currentByte, bitrate);

        if (currentBytePosition > 7) {
            messageBuffer.push(currentByte);
            if (PRINT_TO_STDOUT) {
                System.out.println("-------------------------- Byte:");
                System.out.println(">>>>>> Received: char=" + (char) currentByte.byteValue() + " byte=" + currentByte +
                        " binary=" + HelperUtil.byteToString(currentByte));
            }
            currentBytePosition = 0;
            currentByte = 0;
        }
    }

    /**
     * Extract the LSB from the byte.
     *
     * @param pitch the byte value.
     * @return the LSB of the byte.
     */
    private byte extractBit(byte pitch) {
        return (byte) Math.abs(pitch % 2);
    }

    /**
     * Set the bit in the given byte.
     *
     * @param position the bit position.
     * @param input    the input byte.
     * @param bit      the bit.
     * @return the modified byte.
     */
    private byte setBit(int position, byte input, byte bit) {
        if (bit == 0) {
            return (byte) (input & ~(1 << position));
        } else {
            return (byte) (input | (1 << position));
        }
    }

    /**
     * Check if the stegokey contains the given byte.
     *
     * @param dataByte the byte.
     * @return true if stego contains the given byte.
     */
    private boolean stegoKeyContains(byte dataByte) {
        for (int i = 0; i < stegoKey.length; i++) {
            if (stegoKey[i] == dataByte) {
                return true;
            }
        }
        return false;
    }
}
