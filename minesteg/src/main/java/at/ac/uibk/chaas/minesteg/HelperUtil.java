package at.ac.uibk.chaas.minesteg;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A class that offers some useful methods.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class HelperUtil {
    public static final String DEFAULT_JAVA_ROOT_PATH = "/src/main/java";
    private static String javaRootPath = DEFAULT_JAVA_ROOT_PATH;

    /**
     * Set the root path of the java sources.
     *
     * @param path the path.
     */
    public static void setJavaRootPath(String path) {
        javaRootPath = path;
    }

    /**
     * Search the java source file for the given class.
     *
     * @param classObject   the class to search for.
     * @param classPathRoot the class path root folder.
     * @return the filehandle for the found java source file.
     * @throws FileNotFoundException if no source file was found.
     */
    public static File getFileForClass(Class<?> classObject, String classPathRoot) throws FileNotFoundException {
        String[] classPath = classObject.getName().split("\\.");
        StringBuilder filePath = new StringBuilder(classPath.length * 2 + 1); // a.b.c -> root/a/b/c.java

        filePath.append(classPathRoot);
        for (String path : classPath) {
            filePath.append(File.separator);
            filePath.append(path);
        }
        filePath.append(".java");

        File javaFile = new File(filePath.toString());

        if (!javaFile.exists()) {
            throw new FileNotFoundException(javaFile.getAbsolutePath());
        }

        return javaFile;
    }

    /**
     * Search the java source file for the given class.
     *
     * @param classObject the class to search for.
     * @return the filehandle for the found java source file.
     * @throws FileNotFoundException if no source file was found.
     */
    public static File getFileForClass(Class<?> classObject) throws FileNotFoundException {
        return getFileForClass(classObject, getProjectJavaRootFolder().getAbsolutePath());
    }

    /**
     * Search the java source file for the given class name.
     *
     * @param className the name of the class to search for.
     * @return the filehandle for the found java source file.
     * @throws FileNotFoundException if no source file was found.
     */
    public static File getFileForClass(String className) throws ClassNotFoundException, FileNotFoundException {
        Class<?> classObject = Class.forName(className);

        return getFileForClass(classObject, getProjectJavaRootFolder().getAbsolutePath());
    }

    /**
     * @return the root folder of the java source.
     */
    public static File getProjectJavaRootFolder() {
        Path currentRelativePath = Paths.get("");
        String currentAbsolutePath = currentRelativePath.toAbsolutePath().toString();
        String sourceRoot = currentAbsolutePath + javaRootPath;

        return new File(sourceRoot);
    }

    /**
     * Return the java {@link java.lang.Class} object with the specified class name.
     * <p>
     * This is an "extended" {@link java.lang.Class#forName(java.lang.String) } operation.
     * It is able to return Class objects for primitive types
     * Classes in name space `java.lang` do not need the fully qualified name
     *
     * @param className The class name, never `null`.
     * @throws IllegalArgumentException if no class can be loaded.
     */
    public static Class<?> parseClassType(final String className) {
        switch (className) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "char":
                return char.class;
            case "void":
                return void.class;
            default:
                String fqn = className.contains(".") ? className : "java.lang.".concat(className);
                try {
                    return Class.forName(fqn);
                } catch (ClassNotFoundException ex) {
                    throw new IllegalArgumentException("Class not found: " + fqn);
                }
        }
    }

    /**
     * Convert a byte to binary representation.
     *
     * @param b the byte value to convert.
     * @return a string of 0's and 1's.
     */
    public static String byteToString(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    /**
     * Check if a player is visible (connected to the server) by the client.
     *
     * @param playerName the name of the player.
     * @return true if player is visible, false otherwise.
     */
    public static boolean isPlayerVisible(String playerName) {
        PlayerEntity player = null;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.world != null) {
            for (int i = 0; i < minecraft.world.getPlayers().size(); ++i) {
                PlayerEntity playerentity = minecraft.world.getPlayers().get(i);
                if (playerName.equals(playerentity.getName().getString())) {
                    player = playerentity;
                    break;
                }
            }

            return player != null;
        }

        return false;
    }

    /**
     * Get the reflection field, also check parent classes.
     *
     * @param clazz     the class that should be searched for the field.
     * @param fieldName the field name.
     * @return the reflection field or null if field was not found.
     */
    public static Field findUnderlyingField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;

        if (currentClass == null) {
            return null;
        }

        do {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
            }
        } while ((currentClass = currentClass.getSuperclass()) != null);

        return null;
    }

    /**
     * Convert the angular byte value to float.
     *
     * @param wireData byte value.
     * @return converted float value.
     */
    public static float getServerValue(byte wireData) {
        return wireData * 360.0F / 256.0F;
    }

    /**
     * Convert the angular float value to byte.
     *
     * @param clientValue float value.
     * @return converted byte value.
     */
    public static byte getWireValue(Float clientValue) {
        return (byte) Math.floor(clientValue * 256.0F / 360.0F);
    }

    /**
     * Convert the angular float value to int.
     *
     * @param clientValue float value.
     * @return converted int value.
     */
    public static int getWireValueInt(Float clientValue) {
        return (int) Math.floor(clientValue * 256.0F / 360.0F);
    }

    /**
     * Check if array contains the given byte value.
     *
     * @param array    integer array.
     * @param dataByte needle.
     * @return true if found, false otherwise.
     */
    public static boolean arrayContains(int[] array, byte dataByte) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == dataByte) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the number with the smallest offset to the given needle in the number-array.
     *
     * @param numbers array with numbers.
     * @param number  needle.
     * @param above   if set to true, the higher matching number will be returned.
     * @return the nearest number.
     */
    public static int findNearestNumber(int[] numbers, int number, boolean above) {
        int distanceAbove = Math.abs(numbers[0] - number);
        int distanceBelow = Math.abs(numbers[0] - number);
        int idxBelow = 0;
        int idxAbove = 0;
        for (int c = 1; c < numbers.length; c++) {
            int cdistance = Math.abs(numbers[c] - number);
            if (cdistance < distanceAbove && numbers[c] >= number) {
                idxAbove = c;
                distanceAbove = cdistance;
            }
            if (cdistance < distanceBelow && numbers[c] <= number) {
                idxBelow = c;
                distanceBelow = cdistance;
            }
        }
        return numbers[above ? idxAbove : idxBelow];
    }

    /**
     * Copy a packet by converting it to JSON and then reconverting it back to Java.
     *
     * @param packet     the packet.
     * @param packetType the packet class.
     * @param <T>        the packet class type.
     * @return the copied packet.
     */
    public static <T extends IPacket<?>> T copyPacket(T packet, Class<T> packetType) {
        Gson gson = new Gson();
        T deepCopy = null;

        try {
            deepCopy = gson.fromJson(gson.toJson(packet), packetType);
        } catch (Exception e) {
            System.out.println("Failed to deep copy packet! " + e.getMessage());
        }

        return deepCopy;
    }

    /**
     * Convert a byte value to int.
     *
     * @param b the byte.
     * @return int value.
     */
    public static int btoi(byte b) {
        return b & 0xFF;
    }
}
