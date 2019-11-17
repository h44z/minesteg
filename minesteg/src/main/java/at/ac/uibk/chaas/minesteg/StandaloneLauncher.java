package at.ac.uibk.chaas.minesteg;

import net.minecraft.client.main.Main;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Main entry point that is used in the JAR file to start the game.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class StandaloneLauncher {
    /**
     * Starts a new instance of Minecraft.
     *
     * @param args program arguments.
     */
    public static void main(String[] args) {
        System.out.println("Starting from StandaloneLauncher.main");
        ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));

        if (argList.contains("--assetsDir")) {
            int indexOfArg = argList.indexOf("--assetsDir");
            System.out.println("Assets dir: " + argList.get(indexOfArg + 1));
        }

        Main.main(argList.toArray(new String[]{}));
    }

    /**
     * Concatenate a list.
     *
     * @param first  the first list.
     * @param second the second list.
     * @param <T>    the type of the lists.
     * @return the concatenated list.
     */
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
