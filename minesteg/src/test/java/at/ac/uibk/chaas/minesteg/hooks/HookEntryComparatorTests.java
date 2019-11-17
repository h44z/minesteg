package at.ac.uibk.chaas.minesteg.hooks;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HookEntryComparatorTests {
    private static final Type HOOK_LIST_TYPE = new TypeToken<List<HookEntryJson>>() {}.getType();
    private static final List<HookEntryJson> jsonHookEntries = new ArrayList<>();

    private HookEntryComparator comp = new HookEntryComparator();

    @BeforeAll
    static void beforeAll() {
        Gson gson = new GsonBuilder().create();
        HelperUtil.setJavaRootPath("/src/test/java");
        try (JsonReader reader = new JsonReader(new FileReader(HelperUtil.getProjectJavaRootFolder() + "/../resources/patch_map_comparator.json"))){
            List<HookEntryJson> tmpEntries = gson.fromJson(reader, HOOK_LIST_TYPE);
            jsonHookEntries.addAll(tmpEntries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testEqual() {
        HookEntry first = new HookEntry(jsonHookEntries.get(0));
        HookEntry second = new HookEntry(jsonHookEntries.get(0));
        int result = comp.compare(first, second);
        assertEquals(0, result);
    }

    @Test
    void testGreaterThan() {
        HookEntry first = new HookEntry(jsonHookEntries.get(1));
        HookEntry second = new HookEntry(jsonHookEntries.get(0));
        int result = comp.compare(first, second);
        assertTrue(result >= 1);
    }

    @Test
    void testLessThan() {
        HookEntry first = new HookEntry(jsonHookEntries.get(0));
        HookEntry second = new HookEntry(jsonHookEntries.get(1));
        int result = comp.compare(first, second);
        assertTrue(result <= -1);
    }
}
