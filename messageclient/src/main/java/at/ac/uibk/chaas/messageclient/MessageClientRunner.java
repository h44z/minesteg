package at.ac.uibk.chaas.messageclient;

import at.ac.uibk.chaas.messageclient.rmi.IMessageBufferService;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * The runnable that is called from picocli handler.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
@CommandLine.Command(description = "Stego Message Client.", name = "messageclient",
        mixinStandardHelpOptions = true, version = "messageclient 1.0")
public class MessageClientRunner implements Runnable {
    @CommandLine.Option(names = {"-s", "--server"}, description = "RMI Server", defaultValue = "127.0.0.1")
    private String rmiHost;

    @CommandLine.Option(names = {"-p", "--port"}, description = "RMI Port", defaultValue = "1099")
    private int rmiPort;

    @CommandLine.Option(names = {"-f", "--follow"}, description = "Print buffer content, replace non ascii chars with #")
    private boolean follow;

    @CommandLine.Option(names = {"-a", "--add"}, description = "Append string to buffer")
    private String pushData;

    @CommandLine.Option(names = {"-c", "--clear"}, description = "Clear buffer")
    private boolean clear;

    @CommandLine.Option(names = {"-r", "--read"}, description = "Read buffer")
    private boolean read;

    @CommandLine.Option(names = {"-t", "--readtext"}, description = "Read buffer as text")
    private boolean readText;

    IMessageBufferService rmiService;


    /**
     * Initialize the RMI registry.
     *
     * @throws RemoteException       if an unexpected communication problem occurred.
     * @throws NotBoundException     if the registry was not found.
     * @throws MalformedURLException if a wrong lookup address was specified.
     */
    private void initRMI() throws RemoteException, NotBoundException, MalformedURLException {
        // init security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        rmiService = (IMessageBufferService) Naming.lookup("rmi://" + rmiHost + ":" + rmiPort + "/" +
                IMessageBufferService.class.getSimpleName());
    }

    /**
     * Main entry point for the message client.
     */
    @Override
    public void run() {
        try {
            initRMI();
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        if (follow) {
            boolean isInterrupted = false;
            while (!isInterrupted) {
                try {
                    byte[] bytes = rmiService.getQueuedBytes();
                    for (byte b : bytes) {
                        if (b == (byte) 0xff) {
                            System.out.print("_"); // mark transmission error
                        } else {
                            char chr = (char) b;
                            boolean asciiPrintable = StringUtils.isAsciiPrintable(String.valueOf(chr));
                            if (asciiPrintable) {
                                System.out.print(chr);
                            } else {
                                System.out.print('#'); // mark unprintable packet
                            }
                        }
                    }
                    System.out.println();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    isInterrupted = true;
                }
            }
        } else if (readText) {
            try {
                byte[] bytes = rmiService.getQueuedBytes();
                for (byte b : bytes) {
                    if (b == (byte) 0xff) {
                        System.out.print("_"); // mark transmission error
                    } else {
                        char chr = (char) b;
                        boolean asciiPrintable = StringUtils.isAsciiPrintable(String.valueOf(chr));
                        if (asciiPrintable) {
                            System.out.print(chr);
                        } else {
                            System.out.print('#'); // mark unprintable packet
                        }
                    }
                }
                System.out.println();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (read) {
            try {
                System.out.println(rmiService.getQueuedBytesAsString());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (StringUtils.isNotBlank(pushData)) {
            try {
                rmiService.push(pushData);
                System.out.println("Appended " + pushData.length() + " bytes to buffer!");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (clear) {
            try {
                rmiService.clearQueue();
                System.out.println("Cleared buffer!");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
