package at.ac.uibk.chaas.minesteg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;


public class HelperUtilTests {
    @BeforeEach
    void resetRootPath() {
        HelperUtil.setJavaRootPath(HelperUtil.DEFAULT_JAVA_ROOT_PATH);
    }

    @Test
    void testJavaRootFolder() {
        String workPath = System.getProperty("user.dir");
        workPath += "/src/main/java";

        File sourceRoot = HelperUtil.getProjectJavaRootFolder();


        assertEquals(sourceRoot.getAbsolutePath(), workPath);
    }

    @Test
    void testChangedJavaRootFolder() {
        String workPath = System.getProperty("user.dir");
        workPath += "/src/test/java";

        HelperUtil.setJavaRootPath("/src/test/java");
        File sourceRoot = HelperUtil.getProjectJavaRootFolder();

        assertEquals(sourceRoot.getAbsolutePath(), workPath);
    }

    @Test
    void testFileForClass1() {
        String sourceRoot = HelperUtil.getProjectJavaRootFolder().getAbsolutePath();

        File testFile1 = new File(sourceRoot + "/at/ac/uibk/chaas/minesteg/HelperUtil.java");

        assertTrue(testFile1.exists());

        File testFile2 = null;
        try {
            testFile2 = HelperUtil.getFileForClass(HelperUtil.class, sourceRoot);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assertEquals(testFile1, testFile2);
    }

    @Test
    void testFileForClass2() {
        String sourceRoot = HelperUtil.getProjectJavaRootFolder().getAbsolutePath();

        File testFile1 = null;
        try {
            testFile1 = HelperUtil.getFileForClass(HelperUtil.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assertNotNull(testFile1);


        File testFile2 = null;
        try {
            testFile2 = HelperUtil.getFileForClass(HelperUtil.class, sourceRoot);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assertEquals(testFile1, testFile2);
    }

    @Test
    void testValidClassNameParsing() {
        Class<?> intClass = Integer.class;

        Class<?> parsedClass = HelperUtil.parseClassType("Integer");

        assertEquals(intClass, parsedClass);
    }

    @Test
    void testValidFullClassNameParsing() {
        Class<?> intClass = Integer.class;

        Class<?> parsedClass = HelperUtil.parseClassType("java.lang.Integer");

        assertEquals(intClass, parsedClass);
    }

    @Test
    void testValidPrimitiveClassNameParsing() {
        Class<?> intClass = int.class;

        Class<?> parsedClass = HelperUtil.parseClassType("int");

        assertEquals(intClass, parsedClass);
    }

    @Test
    void testInvalidClassNameParsing() {
        assertThrows(IllegalArgumentException.class, () -> {
            HelperUtil.parseClassType("non.existing.class");
        });
    }

    @Test
    void testInvalidPrimitiveClassNameParsing() {
        assertThrows(IllegalArgumentException.class, () -> {
            HelperUtil.parseClassType("nix");
        });
    }

    @Test
    void testGetWireValue() {
        System.out.println(HelperUtil.getWireValue(0.0f));
        System.out.println(HelperUtil.getWireValue(90.0f));
        System.out.println(HelperUtil.getWireValue(180.0f));
        System.out.println(HelperUtil.getWireValue(270.0f));
        System.out.println(HelperUtil.getWireValue(360.0f));
        System.out.println(HelperUtil.getWireValue(362.0f));
    }

    @Test
    void testNearestNumber() {
        int[] numbers = new int[]{-126, -100, -85, -72, -63, -41, -22, -6, 5, 16, 32, 48, 62, 87, 99, 112, 127};

        int above = HelperUtil.findNearestNumber(numbers, 90, true);
        assertEquals(99, above);

        int below = HelperUtil.findNearestNumber(numbers, 90, false);
        assertEquals(87, below);

        int aboveNeg = HelperUtil.findNearestNumber(numbers, -80, true);
        assertEquals(-72, aboveNeg);

        int belowNeg = HelperUtil.findNearestNumber(numbers, -80, false);
        assertEquals(-85, belowNeg);

        int aboveEqual = HelperUtil.findNearestNumber(numbers, 16, true);
        assertEquals(16, aboveEqual);

        int belowEqual = HelperUtil.findNearestNumber(numbers, 16, false);
        assertEquals(16, belowEqual);
    }

    @Test
    void testIncludedInArrayNumber() {
        int[] stegoKey = new int[]{-126, -100, -85, -72, -63, -51, -44, -30, -22, -11, -6, 5, 16, 26, 32, 48, 55, 62, 87, 99, 112, 126};

        boolean contained = HelperUtil.arrayContains(stegoKey, (byte) -11);

        assertTrue(contained);
    }
}
