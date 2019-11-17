package at.ac.uibk.chaas.minesteg.hooks;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HookEntryTests {
    private static final Type HOOK_LIST_TYPE = new TypeToken<List<HookEntryJson>>() {}.getType();

    private static final List<HookEntryJson> validJsonHookEntries = new ArrayList<>();
    private static final List<HookEntryJson> invalidJsonHookEntries = new ArrayList<>();

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

        Gson gson = new GsonBuilder().create();
        try (JsonReader reader = new JsonReader(new FileReader(HelperUtil.getProjectJavaRootFolder() + "/../resources/patch_map_test_valid.json"))){
            List<HookEntryJson> tmpEntries = gson.fromJson(reader, HOOK_LIST_TYPE);
            validJsonHookEntries.addAll(tmpEntries);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (JsonReader reader = new JsonReader(new FileReader(HelperUtil.getProjectJavaRootFolder() + "/../resources/patch_map_test_invalid.json"))){
            List<HookEntryJson> tmpEntries = gson.fromJson(reader, HOOK_LIST_TYPE);
            invalidJsonHookEntries.addAll(tmpEntries);
        } catch (IOException e) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    void validEntry1() {
        HookEntry validHookEntry = new HookEntry(validJsonHookEntries.get(0));
        assertTrue(validHookEntry.isValid());
    }

    @Test
    void validEntry2() {
        HookEntry validHookEntry = new HookEntry(validJsonHookEntries.get(1));
        assertTrue(validHookEntry.isValid());
    }


    @Test
    void validEntry3() {
        HookEntry validHookEntry = new HookEntry(validJsonHookEntries.get(2));
        assertTrue(validHookEntry.isValid());
    }

    @Test
    void validEntry4() {
        HookEntry validHookEntry = new HookEntry(validJsonHookEntries.get(3));
        assertTrue(validHookEntry.isValid());
    }

    @Test
    void invalidEntry1() {
        HookEntry invalidHookEntry = new HookEntry(invalidJsonHookEntries.get(0));
        assertFalse(invalidHookEntry.isValid());
    }

    @Test
    void invalidEntry2() {
        HookEntry invalidHookEntry = new HookEntry(invalidJsonHookEntries.get(1));
        assertFalse(invalidHookEntry.isValid());
    }

    @Test
    void invalidEntry3() {
        HookEntry invalidHookEntry = new HookEntry(invalidJsonHookEntries.get(2));
        assertFalse(invalidHookEntry.isValid());
    }

    @Test
    void invalidEntry4() {
        HookEntry invalidHookEntry = new HookEntry(invalidJsonHookEntries.get(3));
        assertFalse(invalidHookEntry.isValid());
    }

    @Test
    void injectionTest1() {
        HookEntry validHookEntry = new HookEntry(validJsonHookEntries.get(0));

        assertDoesNotThrow(() -> validHookEntry.patchDestination(0));

        try {
            assertEquals(Files.lines(sourceFile.toPath()).count(), Files.lines(backupFile.toPath()).count() + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void injectionTest2() {
        HookEntry validHookEntry = new HookEntry(validJsonHookEntries.get(0));

        assertDoesNotThrow(() -> validHookEntry.patchDestination(0));

        try {
            assertEquals(Files.lines(sourceFile.toPath()).count(), Files.lines(backupFile.toPath()).count() + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void injectionTest3() {
        HookEntry validHookEntry = new HookEntry(validJsonHookEntries.get(1));

        assertDoesNotThrow(() -> validHookEntry.patchDestination(0));

        try {
            assertEquals(Files.lines(sourceFile.toPath()).count(), Files.lines(backupFile.toPath()).count() + 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void injectionTest4() {
        HookEntry validHookEntry1 = new HookEntry(validJsonHookEntries.get(2));
        HookEntry validHookEntry2 = new HookEntry(validJsonHookEntries.get(3));

        assertDoesNotThrow(() -> validHookEntry1.patchDestination(0));
        assertDoesNotThrow(() -> validHookEntry2.patchDestination(1));

        try {
            assertEquals(Files.lines(sourceFile.toPath()).count(), Files.lines(backupFile.toPath()).count() + 4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
