package at.ac.uibk.chaas.minesteg.steg;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import at.ac.uibk.chaas.minesteg.analyze.EncoderPacketLogger;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Random;

import static at.ac.uibk.chaas.minesteg.steg.SharedPreferences.INIT_DELAY_SENDER_MS;
import static at.ac.uibk.chaas.minesteg.steg.SharedPreferences.MIN_REPEAT_COUNT;

/**
 * The encoder implementation for the steganographic system.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class Encoder {
    private static final boolean PRINT_TO_STDOUT = true;

    private final MessageBuffer messageBuffer;
    private final String receiverName;
    private final int[] stegoKey;
    private final Random random = new Random(new Date().getTime());
    private final EncoderPacketLogger logger;

    private int currentBytePosition = 0;
    private int sendCounter = 0;
    private int syncCountDown = SharedPreferences.SYNC_INTERVAL;
    private boolean initialized = false;
    private int initialGarbageCounter = 0;
    private boolean initialGarbageProcessed = false;
    private long firstConnMs = 0;
    private Byte byteToSend = null;

    private float pitchTrend = 0; // pos -> increasing, neg -> decreasing
    private float yawTrend = 0;
    CircularFifoQueue<Float> pitchHistory = new CircularFifoQueue<>(SharedPreferences.HISTORY_LENGTH);
    CircularFifoQueue<Float> yawHistory = new CircularFifoQueue<>(SharedPreferences.HISTORY_LENGTH);
    private float pitchOnWire = 0;
    private float yawOnWire = 0;
    private float validPitchOnWire = 0;
    private float validYawOnWire = 0;

    /**
     * Constructor.
     *
     * @param messageBuffer the message buffer instance. This buffer contains the decoded secret message.
     * @param receiverName  the name of the decoding player.
     * @param stegoKey      the stegokey.
     */
    public Encoder(MessageBuffer messageBuffer, String receiverName, int[] stegoKey) {
        this.messageBuffer = messageBuffer;
        this.receiverName = receiverName;
        this.stegoKey = stegoKey;

        for (int i = 0; i < SharedPreferences.HISTORY_LENGTH; i++) {
            pitchHistory.add(0.0f);
            yawHistory.add(0.0f);
        }

        logger = new EncoderPacketLogger();
    }

    /**
     * @return true if initialization of the encoder is completed.
     */
    private boolean isInitialized() {
        if (initialized) {
            return true;
        }

        if (firstConnMs == 0) {
            firstConnMs = System.currentTimeMillis();
            return false;
        }

        long currTimeMs = System.currentTimeMillis();
        if (currTimeMs > firstConnMs + INIT_DELAY_SENDER_MS) {
            initialized = true;

            System.out.println("Init done! " + currTimeMs);
            System.out.println("Sending: " + messageBuffer.toBitString());
            return true;
        }

        return false;
    }

    /**
     * Main entry point for the encoder. This method is called for every send packet.
     *
     * @param packet the outgoing packet.
     */
    public void processPacket(IPacket<?> packet) {
        if (!HelperUtil.isPlayerVisible(receiverName)) {
            currentBytePosition = 0;
            sendCounter = 0;
            initialized = false;
            initialGarbageCounter = 0;
            initialGarbageProcessed = false;
            firstConnMs = 0;
            syncCountDown = SharedPreferences.SYNC_INTERVAL;

            return; // cannot process packet
        }

        if (!isInitialized()) {
            return;
        }

        if (!isModifiablePacket(packet)) {
            return; // cannot process packet
        }

        CPlayerPacket origPacket = HelperUtil.copyPacket((CPlayerPacket) packet, CPlayerPacket.class);
        String packetType;

        if (PRINT_TO_STDOUT) {
            System.out.println("-------------------------- Packet:");
        }
        // Init byte to send
        if (byteToSend == null) {
            byteToSend = messageBuffer.get(); // read byte
        }

        byte bitPitch = getBit(currentBytePosition, byteToSend);
        byte bitYaw = getBit(currentBytePosition + 1, byteToSend);


        // Store original values
        pitchHistory.add(((CPlayerPacket) packet).getPitch(0));
        yawHistory.add(((CPlayerPacket) packet).getYaw(0));

        assert ((CPlayerPacket) packet).getYaw(0) == getCurrentYaw();
        assert ((CPlayerPacket) packet).getPitch(0) == getCurrentPitch();

        calculateTrend();

        if (PRINT_TO_STDOUT) {
            /*System.out.println("Modifying packet: P = " + pitchHistory.get(SharedPreferences.HISTORY_LENGTH - 1) +
                    " Y = " + yawHistory.get(SharedPreferences.HISTORY_LENGTH - 1));*/
            System.out.println("Trend: P = " + pitchTrend + " Y = " + yawTrend);
        }

        // Calculate values to send
        if (sendCounter == 0 && (syncCountDown == 0 || syncCountDown == 1 || syncCountDown == 2)) {
            initialGarbageProcessed = true;
            currentBytePosition = 0;
            sendCounter = 0;
            firstConnMs = 0;

            if (syncCountDown == 0) {
                syncCountDown = SharedPreferences.SYNC_INTERVAL;
            } else {
                syncCountDown--;
            }

            // sync packet: send either pitch or yaw from stego key
            float pitchInStegoKey = getRandomPitchInStegoKey();
            float yawInStegoKey = getRandomYawInStegoKey();

            float pitchDiff = Math.abs(pitchInStegoKey - getCurrentPitch());
            float yawDiff = Math.abs(yawInStegoKey - getCurrentYaw());
            boolean randomizePitch = pitchDiff <= yawDiff;

            if (randomizePitch) {
                pitchOnWire = pitchInStegoKey;
                if (!HelperUtil.arrayContains(stegoKey, HelperUtil.getWireValue(getCurrentYaw()))) {
                    yawOnWire = getCurrentYaw() == null ? 0 : getCurrentYaw();
                } else {
                    yawOnWire = getYawNotInStegoKey();
                }
            } else {
                yawOnWire = yawInStegoKey;
                if (!HelperUtil.arrayContains(stegoKey, HelperUtil.getWireValue(getCurrentPitch()))) {
                    pitchOnWire = getCurrentPitch() == null ? 0 : getCurrentPitch();
                } else {
                    pitchOnWire = getPitchNotInStegoKey();
                }
            }

            if (PRINT_TO_STDOUT) {
                System.out.println("Sync packet: P=" + HelperUtil.getWireValue(pitchOnWire) +
                        " Y=" + HelperUtil.getWireValue(yawOnWire));
            }
            packetType = "sync" + (randomizePitch ? "_pitch" : "_yaw");
        } else if (initialGarbageCounter <= stegoKey.length) {
            // sync packet: send either pitch or yaw from stego key
            float pitchInStegoKey = getRandomPitchInStegoKey();
            float yawInStegoKey = getRandomYawInStegoKey();

            float pitchDiff = Math.abs(pitchInStegoKey - getCurrentPitch());
            float yawDiff = Math.abs(yawInStegoKey - getCurrentYaw());
            boolean randomizePitch = pitchDiff <= yawDiff;

            if (randomizePitch) {
                pitchOnWire = pitchInStegoKey;
                if (!HelperUtil.arrayContains(stegoKey, HelperUtil.getWireValue(getCurrentYaw()))) {
                    yawOnWire = getCurrentYaw() == null ? 0 : getCurrentYaw();
                } else {
                    yawOnWire = getYawNotInStegoKey();
                }
            } else {
                yawOnWire = yawInStegoKey;
                if (!HelperUtil.arrayContains(stegoKey, HelperUtil.getWireValue(getCurrentPitch()))) {
                    pitchOnWire = getCurrentPitch() == null ? 0 : getCurrentPitch();
                } else {
                    pitchOnWire = getPitchNotInStegoKey();
                }
            }

            if (initialGarbageCounter < stegoKey.length) {
                packetType = "sync_initial" + (randomizePitch ? "_pitch" : "_yaw");
                initialGarbageCounter++;
            } else if (!initialGarbageProcessed) {
                packetType = "sync_initial" + (randomizePitch ? "_pitch" : "_yaw");
                syncCountDown = 2; // send sync after initial garbage
                initialGarbageCounter++;
            } else {
                packetType = "sync" + (randomizePitch ? "_pitch" : "_yaw");
            }

            if (PRINT_TO_STDOUT) {
                System.out.println("Sync packet: P=" + HelperUtil.getWireValue(pitchOnWire) +
                        " Y=" + HelperUtil.getWireValue(yawOnWire) + " (" + (randomizePitch ? "pitch)" : "yaw)"));
            }
        } else {
            if (sendCounter == 0) {
                if (currentBytePosition == 0) {
                    if (PRINT_TO_STDOUT) {
                        System.out.println(">>>>>> Sending: char=" + (char) byteToSend.byteValue() + " byte=" + byteToSend +
                                " binary=" + HelperUtil.byteToString(byteToSend));
                    }
                }
                pitchOnWire = roundToBitPitch(bitPitch);
                yawOnWire = roundToBitYaw(bitYaw);
            } else {
                // re-send the previous data
                pitchOnWire = randomizePitch(pitchOnWire);
                yawOnWire = randomizeYaw(yawOnWire);
            }

            validPitchOnWire = pitchOnWire;
            validYawOnWire = yawOnWire;

            sendCounter++;
            syncCountDown--;

            if (PRINT_TO_STDOUT) {
                System.out.println("Valid packet: P=" + HelperUtil.getWireValue(pitchOnWire) +
                        " Y=" + HelperUtil.getWireValue(yawOnWire) + " | pos=" + currentBytePosition);
                System.out.println("P = " + HelperUtil.getWireValue(pitchOnWire) + " | " + bitPitch + " | " + sendCounter + " | " + pitchHistory.get(SharedPreferences.HISTORY_LENGTH - 1) + " | " + pitchOnWire);
                System.out.println("Y = " + HelperUtil.getWireValue(yawOnWire) + " | " + bitYaw + " | " + sendCounter + " | " + yawHistory.get(SharedPreferences.HISTORY_LENGTH - 1) + " | " + yawOnWire);
            }
            packetType = "valid";
        }

        // Modify package
        Field fieldPitch = HelperUtil.findUnderlyingField(packet.getClass(), "pitch");
        Field fieldYaw = HelperUtil.findUnderlyingField(packet.getClass(), "yaw");

        if (fieldPitch == null || fieldYaw == null) {
            throw new RuntimeException("FATAL: fields not found in class!");
        }

        boolean wasAccessiblePitch = fieldPitch.isAccessible();
        boolean wasAccessibleYaw = fieldYaw.isAccessible();
        fieldPitch.setAccessible(true);
        fieldYaw.setAccessible(true);
        try {
            fieldPitch.set(packet, pitchOnWire);
            fieldYaw.set(packet, yawOnWire);
        } catch (IllegalAccessException e) {
            System.out.println("Failed to set fields: " + e.getMessage());
        }

        fieldPitch.setAccessible(wasAccessiblePitch);
        fieldYaw.setAccessible(wasAccessibleYaw);

        logger.logPacket(origPacket, (CPlayerPacket) packet, packetType, bitPitch, bitYaw, sendCounter, currentBytePosition, byteToSend);

        if (sendCounter > MIN_REPEAT_COUNT) {
            sendCounter = 0;
            currentBytePosition += 2;
        }

        if (currentBytePosition > 7) {
            currentBytePosition = 0;
            byteToSend = messageBuffer.get(); // remove the already sent entry
        }
    }

    /**
     * Extract the bit from the byte at a given position.
     *
     * @param position the bit position.
     * @param b        the byte.
     * @return the bit.
     */
    private byte getBit(int position, byte b) {
        return (byte) ((b >> position) & 1);
    }

    /**
     * Check if the packet is valid for embedding data.
     *
     * @param packet the outgoing packet.
     * @return true if data can be embedded.
     */
    private boolean isModifiablePacket(IPacket<?> packet) {
        return packet instanceof CPlayerPacket.RotationPacket || packet instanceof CPlayerPacket.PositionRotationPacket;
    }

    /**
     * @return a random yaw value that is included in the stegokey.
     */
    private float getRandomYawInStegoKey() {
        // find best matching pitch in stegokey
        byte currentYawWire = HelperUtil.getWireValue(getCurrentYaw());

        int nearestStegoKeyValue = HelperUtil.findNearestNumber(stegoKey, currentYawWire, yawTrend >= 0);

        return convertWireYaw((byte) nearestStegoKeyValue, getCurrentYaw());
    }

    /**
     * @return a random pitch value that is included in the stegokey.
     */
    private float getRandomPitchInStegoKey() {
        // find best matching pitch in stegokey
        byte currentPitchWire = HelperUtil.getWireValue(getCurrentPitch());

        int nearestStegoKeyValue = HelperUtil.findNearestNumber(stegoKey, currentPitchWire, pitchTrend >= 0);

        return convertWirePitch((byte) nearestStegoKeyValue);
    }

    /**
     * @return a random pitch value that is not included in the stegokey.
     */
    private Float getPitchNotInStegoKey() {
        // find best matching pitch in stegokey
        byte currentPitchWire = HelperUtil.getWireValue(getCurrentPitch());

        if (!HelperUtil.arrayContains(stegoKey, currentPitchWire)) {
            return getCurrentPitch();
        }

        if (pitchTrend >= 0) {
            currentPitchWire += 1;
        } else {
            currentPitchWire -= 1;
        }

        // Pitch must be between -90 and 90 degrees!
        if (currentPitchWire > 64) {
            currentPitchWire = 64;
        } else if (currentPitchWire < -64) {
            currentPitchWire = -64;
        }

        return convertWirePitch(currentPitchWire);
    }

    /**
     * @return a random yaw value that is not included in the stegokey.
     */
    private Float getYawNotInStegoKey() {
        // find best matching pitch in stegokey
        int currentYawWire = HelperUtil.getWireValueInt(getCurrentYaw());

        if (!HelperUtil.arrayContains(stegoKey, (byte) currentYawWire)) {
            return getCurrentYaw();
        }

        if (yawTrend >= 0) {
            currentYawWire += 1;
        } else {
            currentYawWire -= 1;
        }

        return convertWireYaw(currentYawWire);
    }

    /**
     * Calculate the movement direction trend.
     */
    private void calculateTrend() {
        pitchTrend = 0;
        yawTrend = 0;

        for (int i = pitchHistory.maxSize() - 1; i > 0; --i) {
            float p = pitchHistory.get(i) == null ? 0 : pitchHistory.get(i);
            float pOld = pitchHistory.get(i - 1) == null ? 0 : pitchHistory.get(i - 1);
            pitchTrend += (p - pOld) * 1 / i;
        }

        for (int i = yawHistory.maxSize() - 1; i > 0; --i) {
            float y = yawHistory.get(i) == null ? 0 : yawHistory.get(i);
            float yOld = yawHistory.get(i - 1) == null ? 0 : yawHistory.get(i - 1);
            yawTrend += (y - yOld) * 1 / i;
        }
    }

    /**
     * Get a float value for the given pitch byte.
     * This is the main embedding method for pitch.
     *
     * @param bit the pitch.
     * @return a float value for pitch.
     */
    private float roundToBitPitch(byte bit) {
        byte pitchWire = HelperUtil.getWireValue(getCurrentPitch());
        byte prevPitchWire = HelperUtil.getWireValue(validPitchOnWire);

        boolean pitchChangeNeeded = (pitchWire % 2 == 0 && bit == 1) || (pitchWire % 2 != 0 && bit == 0);
        if (pitchTrend >= 0) {
            if (pitchChangeNeeded) {
                pitchWire += 1;
            }

            // we need to have different values for every pitch we send
            if (pitchWire == prevPitchWire) {
                pitchWire += 2;
            }
            if (HelperUtil.arrayContains(stegoKey, pitchWire)) {
                pitchWire += 2;
            }
        } else {
            if (pitchChangeNeeded) {
                pitchWire -= 1;
            }

            // we need to have different values for every pitch we send
            if (pitchWire == prevPitchWire) {
                pitchWire -= 2;
            }
            if (HelperUtil.arrayContains(stegoKey, pitchWire)) {
                pitchWire -= 2;
            }
        }

        // Pitch must be between -90 and 90 degrees!
        if (pitchWire > 64) {
            int diff = 2;
            if (prevPitchWire - pitchWire == -2) {
                diff = 4; // prev pitch has a distance of two, we need to subtract 4 in this case
            }

            pitchWire -= diff;
        } else if (pitchWire < -64) {
            int diff = 2;
            if (prevPitchWire - pitchWire == 2) {
                diff = 4; // prev pitch has a distance of two, we need to add 4 in this case
            }

            pitchWire += diff;
        }

        return convertWirePitch(pitchWire);
    }

    /**
     * Get a float value for the given yaw byte.
     * This is the main embedding method for yaw.
     *
     * @param bit the yaw.
     * @return a float value for yaw.
     */
    private float roundToBitYaw(byte bit) {
        int yawWire = HelperUtil.getWireValueInt(getCurrentYaw());
        int prevYawWire = HelperUtil.getWireValueInt(validYawOnWire);

        boolean yawChangeNeeded = (yawWire % 2 == 0 && bit == 1) || (yawWire % 2 != 0 && bit == 0);
        if (yawTrend >= 0) {
            if (yawChangeNeeded) {
                yawWire += 1;
            }

            // we need to have different values for every yaw we send
            if (yawWire == prevYawWire) {
                yawWire += 2;
            }

            if (HelperUtil.arrayContains(stegoKey, (byte) yawWire)) {
                yawWire += 2;
            }
        } else {
            if (yawChangeNeeded) {
                yawWire -= 1;
            }

            // we need to have different values for every yaw we send
            if (yawWire == prevYawWire) {
                yawWire -= 2;
            }

            if (HelperUtil.arrayContains(stegoKey, (byte) yawWire)) {
                yawWire -= 2;
            }
        }

        return convertWireYaw(yawWire);
    }

    /**
     * Convert the pitch byte to float. Randomize the float value a little.
     *
     * @param wirePitch the byte value for pitch.
     * @return the float value for pitch.
     */
    protected float convertWirePitch(byte wirePitch) {
        float value = wirePitch * 360.0F / 256.0F;

        return randomizePitch(value);
    }

    /**
     * Convert the yaw byte to float. Randomize the float value a little.
     *
     * @param wireYaw the byte value for yaw.
     * @return the float value for yaw.
     */
    protected float convertWireYaw(int wireYaw) {
        float value = wireYaw * 360.0F / 256.0F;
        return randomizeYaw(value);
    }

    /**
     * Convert the yaw byte to float. Randomize the float value a little.
     *
     * @param wireYaw the byte value for yaw.
     * @param origYaw the original yaw value.
     * @return the float value for yaw.
     */
    protected float convertWireYaw(byte wireYaw, float origYaw) {
        int wireYawInt = HelperUtil.btoi(wireYaw);
        int wireYawOrigInt = HelperUtil.getWireValueInt(origYaw);
        int sign = 1;
        if (origYaw < 0) {
            wireYawInt -= 256;
            sign = -1;
        }

        int multiplier = wireYawOrigInt / 256;

        int wireYawInt1 = wireYawInt + 256 * multiplier;
        int wireYawInt2 = wireYawInt + 256 * (multiplier - sign);
        int wireYawInt3 = wireYawInt + 256 * (multiplier + sign);

        int diff1 = Math.abs(wireYawOrigInt - wireYawInt1);
        int diff2 = Math.abs(wireYawOrigInt - wireYawInt2);
        int diff3 = Math.abs(wireYawOrigInt - wireYawInt3);

        int min = Math.min(diff1, Math.min(diff2, diff3));

        if (min == diff1) {
            wireYawInt = wireYawInt1;
        } else if (min == diff2) {
            wireYawInt = wireYawInt2;
        } else {
            wireYawInt = wireYawInt3;
        }

        float value = wireYawInt * 360.0F / 256.0F;

        return randomizeYaw(value);
    }

    /**
     * @return the current yaw value.
     */
    private Float getCurrentYaw() {
        return yawHistory.get(SharedPreferences.HISTORY_LENGTH - 1);
    }

    /**
     * @return the current pitch value.
     */
    private Float getCurrentPitch() {
        return pitchHistory.get(SharedPreferences.HISTORY_LENGTH - 1);
    }

    /**
     * Randomize the pitch value. The randomized value still converts to the
     * same byte value as the original value.
     *
     * @param value the original value.
     * @return the randomize value.
     */
    protected float randomizePitch(float value) {
        int encodedValue = HelperUtil.getWireValueInt(value);

        float min = encodedValue * 360.0F / 256.0F;
        float max = ((encodedValue + 1) * 360.0F / 256.0F) - 0.000001F;

        if (max > 90.0F) {
            max = 90.0F;
        }
        if (min < -90.0F) {
            min = -90.0F;
        }

        return min + random.nextFloat() * (max - min);
    }

    /**
     * Randomize the yaw value. The randomized value still converts to the
     * same byte value as the original value.
     *
     * @param value the original value.
     * @return the randomize value.
     */
    protected float randomizeYaw(float value) {
        int encodedValue = HelperUtil.getWireValueInt(value);

        float min = encodedValue * 360.0F / 256.0F;
        float max = ((encodedValue + 1) * 360.0F / 256.0F) - 0.0001F;
        return min + random.nextFloat() * (max - min);
    }
}
