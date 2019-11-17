package at.ac.uibk.chaas.minesteg.rmi;

import at.ac.uibk.chaas.messageclient.rmi.IMessageBufferService;
import at.ac.uibk.chaas.minesteg.steg.MessageBuffer;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessControlException;

/**
 * A class that handles the configuration of the embedded RMI server.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class RMIServer {
    final MessageBufferService messageBufferService;
    final int port;

    /**
     * Constructor.
     *
     * @param messageBuffer the message buffer instance.
     * @param port          the port that is used for the RMI registry.
     */
    public RMIServer(MessageBuffer messageBuffer, int port) {
        this.messageBufferService = new MessageBufferService(messageBuffer);
        this.port = port;
    }

    /**
     * Startup the RMI server.
     */
    public void run() {
        try {
            Registry registry;

            // init security manager
            /*if(System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }*/

            try {
                registry = LocateRegistry.getRegistry(port);
                registry.list(); // This call will throw an exception if the registry does not already exist
            } catch (RemoteException e) {
                registry = null;
            }

            if (registry == null) {
                try {
                    registry = LocateRegistry.createRegistry(port);
                } catch (RemoteException e) {
                    System.out.println("Failed to create registry at port " + port + ": " + e.getMessage());
                }
            }

            if (registry != null) {
                IMessageBufferService stub = (IMessageBufferService) UnicastRemoteObject.exportObject(messageBufferService, 0);

                // Bind the remote object's stub in the registry
                try {
                    registry.bind(IMessageBufferService.class.getSimpleName(), stub);
                    System.out.println("Bound " + IMessageBufferService.class.getSimpleName() + " to registry");
                } catch (RemoteException e) {
                    System.out.println("Failed to bind: " + e.getMessage());
                } catch (AlreadyBoundException e) {
                    System.out.println("Object is alrady bound: " + e.getMessage());
                } catch (AccessControlException e) {
                    System.out.println("Security exception: " + e.getMessage());

                    System.out.println("Generate a file $HOME/.java.policy: grant {permission java.security.AllPermission;};");
                }
            }
            System.out.println("RMI Server ready");
        } catch (Exception e) {
            System.err.println("RMI Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
