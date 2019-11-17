package at.ac.uibk.chaas.minesteg.rmi;

import at.ac.uibk.chaas.messageclient.rmi.IMessageBufferService;
import at.ac.uibk.chaas.minesteg.steg.MessageBuffer;

/**
 * The message buffer service implementation.
 * This methods can be called via RMI.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class MessageBufferService implements IMessageBufferService {
    MessageBuffer messageBuffer;

    /**
     * Constructor.
     *
     * @param messageBuffer the message buffer instance.
     */
    public MessageBufferService(MessageBuffer messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    /**
     * @return the count of queued bytes in the message buffer.
     */
    @Override
    public int getQueuedByteCount() {
        return messageBuffer.size();
    }

    /**
     * @return the queued bytes in the message buffer as string.
     */
    @Override
    public String getQueuedBytesAsString() {
        return messageBuffer.toString();
    }

    /**
     * @return the queued bytes in the message buffer as byte array.
     */
    @Override
    public byte[] getQueuedBytes() {
        return messageBuffer.toBytes();
    }

    /**
     * Clear the buffer.
     */
    @Override
    public void clearQueue() {
        messageBuffer.clear();
    }

    /**
     * Append the given string to the message buffer.
     *
     * @param data the message.
     */
    @Override
    public void push(String data) {
        messageBuffer.push(data);
    }

    /**
     * Append the given bytes to the message buffer.
     *
     * @param data the bytes.
     */
    @Override
    public void push(byte[] data) {
        for (byte b : data) {
            messageBuffer.push(b);
        }
    }
}
