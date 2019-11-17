package at.ac.uibk.chaas.minesteg.hooks;

/**
 * A class that contains some methods that have been used during hook injector tests.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class TestClass {
    public static void printHello() {
        System.out.println("Hello Minecraft!");
    }

    public static void printHelloWithArgs(int i) {
        System.out.println("Hello Minecraft! " + i);
    }

    public static void printHelloWithArgs2(int i, int j) {
        System.out.println("Hello Minecraft! " + i + "; " + j);
    }
}
