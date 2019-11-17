package at.ac.uibk.chaas.minesteg.hooks;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class HookInjectorTests {
    private static HookInjector validInjector;
    private static HookInjector invalidInjector;

    private static File sourceFile;
    private static File backupFile;

    @BeforeAll
    static void beforeAll() {
        HelperUtil.setJavaRootPath("/src/test/java");
        try {
            sourceFile = HelperUtil.getFileForClass(HookEntryTestForPatching.class);
            backupFile = new File(sourceFile.getAbsolutePath() + ".testbak");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            validInjector = new HookInjector(System.getProperty("user.dir") + "/src/test/resources/patch_map_test_valid.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            invalidInjector = new HookInjector(System.getProperty("user.dir") + "/src/test/resources/patch_map_test_invalid.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // backup original HookEntryTestFile
        try {
            Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void afterAll() {
        // restore original HookEntryTestFile
        try {
            Files.copy(backupFile.toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.delete(backupFile.toPath());

            // delete injector backup file
            Files.delete(Paths.get(sourceFile.getAbsolutePath() + ".origbak"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    void testCreation() {
        assertNotNull(validInjector);
    }

    @Test
    void testCreationFailure() {
        assertThrows(FileNotFoundException.class, () -> {
            new HookInjector(System.getProperty("user.dir") + "/src/test/resources/nosuchfile.json");
        });
    }

    @Test
    void testValidInjection() {
        validInjector.injectHooks();

        try {
            assertEquals(Files.lines(sourceFile.toPath()).count(), Files.lines(backupFile.toPath()).count() + 4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testInvalidInjection() {
        invalidInjector.injectHooks();

        try {
            assertEquals(Files.lines(sourceFile.toPath()).count(), Files.lines(backupFile.toPath()).count());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
