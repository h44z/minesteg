package at.ac.uibk.chaas.messageclient;

import picocli.CommandLine;

/**
 * The main class that gets called in the JAR file.
 * 
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class MessageClient {
    /**
     * Main entry point starts the picocli handler that processes the input arguments.
     *
     * @param args the program arguments.
     */
    public static void main(String[] args) {
        CommandLine.run(new MessageClientRunner(), System.err, args);
    }
}
