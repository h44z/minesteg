package at.ac.uibk.chaas.minesteg.steg;

import at.ac.uibk.chaas.minesteg.HelperUtil;

import java.util.LinkedList;

/**
 * Thread safe FIFO Queue.
 * This queue is used to store the embedded/source secret message.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class MessageBuffer {
    private LinkedList<Byte> byteFifoQueue = new LinkedList<>();

    /**
     * Constructor.
     */
    public MessageBuffer() {

    }

    /**
     * Constructor.
     *
     * @param msg initialize the queue with the given message.
     */
    public MessageBuffer(String msg) {
        push(msg);
    }

    /**
     * @return the first byte in the queue and remove it.
     */
    public synchronized byte get() {
        if (byteFifoQueue.isEmpty()) {
            return 0; // by default get a null byte
        }

        return byteFifoQueue.removeFirst();
    }

    /**
     * @return the first byte in the queue but keep it queued.
     */
    public synchronized byte read() {
        if (byteFifoQueue.isEmpty()) {
            return 0; // by default get a null byte
        }

        return byteFifoQueue.getFirst();
    }

    /**
     * Append the given byte to the queue.
     *
     * @param b the byte.
     */
    public synchronized void push(byte b) {
        byteFifoQueue.add(b);
    }

    /**
     * Add a ascii string to the queue.
     *
     * @param str
     */
    public synchronized void push(String str) {
        for (char c : str.toCharArray()) {
            push((byte) c);
        }
    }

    /**
     * @return the byte queue as string.
     */
    public synchronized String toString() {
        StringBuilder out = new StringBuilder();

        for (byte b : byteFifoQueue) {
            out.append((char) b);
        }

        return out.toString();
    }

    /**
     * @return the byte queue as byte array.
     */
    public synchronized byte[] toBytes() {
        byte[] bytes = new byte[byteFifoQueue.size()];

        for (int i = 0; i < byteFifoQueue.size(); ++i) {
            bytes[i] = byteFifoQueue.get(i);
        }

        return bytes;
    }

    /**
     * @return the byte queue as string in binary representation.
     */
    public synchronized String toBitString() {
        StringBuilder out = new StringBuilder();

        for (byte b : byteFifoQueue) {
            out.append(HelperUtil.byteToString(b));
        }

        return out.toString();
    }

    /**
     * @return the current size of the queue.
     */
    public int size() {
        return byteFifoQueue.size();
    }

    /**
     * Clear the queue.
     */
    public synchronized void clear() {
        byteFifoQueue.clear();
    }
}