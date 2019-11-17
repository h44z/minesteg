package at.ac.uibk.chaas.messageclient.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The message buffer service interface.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public interface IMessageBufferService extends Remote {
    /**
     * @return the count of queued bytes in the message buffer.
     */
    int getQueuedByteCount() throws RemoteException;

    /**
     * @return the queued bytes in the message buffer as string.
     */
    String getQueuedBytesAsString() throws RemoteException;

    /**
     * @return the queued bytes in the message buffer as byte array.
     */
    byte[] getQueuedBytes() throws RemoteException;

    /**
     * Clear the buffer.
     */
    void clearQueue() throws RemoteException;

    /**
     * Append the given string to the message buffer.
     *
     * @param data the message.
     */
    void push(String data) throws RemoteException;

    /**
     * Append the given bytes to the message buffer.
     *
     * @param data the bytes.
     */
    void push(byte[] data) throws RemoteException;
}
