package at.ac.uibk.chaas.minesteg.hooks;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * The hook injector implementation for the Minecraft source code.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class HookInjector {
    private final Logger logger = LogManager.getLogger();

    private static final Type HOOK_LIST_TYPE = new TypeToken<List<HookEntryJson>>() {
    }.getType();

    private File patchFile;
    private List<HookEntry> hookEntries;
    private Map<String, List<HookEntry>> entriesPerFile;

    /**
     * Constructor.
     *
     * @param patchFileName the configuration filename for the injector.
     * @throws FileNotFoundException if config file was not found.
     */
    public HookInjector(String patchFileName) throws FileNotFoundException {
        patchFile = new File(patchFileName);

        if (!patchFile.exists()) {
            throw new FileNotFoundException(patchFileName);
        }

        parsePathFile();
        createGroupedInjectionMap();
    }

    /**
     * Parse the given configuration file.
     */
    private void parsePathFile() {
        Gson gson = new GsonBuilder().create();
        try (JsonReader reader = new JsonReader(new FileReader(patchFile))) {
            List<HookEntryJson> jsonHookEntries = gson.fromJson(reader, HOOK_LIST_TYPE);
            hookEntries = new ArrayList<>(jsonHookEntries.size());
            jsonHookEntries.forEach(json -> hookEntries.add(new HookEntry(json)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a injection map including all hooks, grouped by destination class.
     */
    private void createGroupedInjectionMap() {
        entriesPerFile = new HashMap<>();

        hookEntries.forEach(hookEntry -> {
            if (entriesPerFile.containsKey(hookEntry.getDestinationClassName())) {
                entriesPerFile.get(hookEntry.getDestinationClassName()).add(hookEntry);
            } else {
                entriesPerFile.put(hookEntry.getDestinationClassName(), new ArrayList<>(Collections.singletonList(hookEntry)));
            }
        });

        entriesPerFile.forEach((file, hookEntriesPerFile) -> hookEntriesPerFile.sort(new HookEntryComparator()));
    }

    /**
     * Inject the hooks to the Minecraft source code.
     */
    public void injectHooks() {
        entriesPerFile.forEach((file, hookEntriesPerFile) -> {
            createOrRestoreBackup(file);

            int patchCounter = 0;
            for (HookEntry hookEntry : hookEntriesPerFile) {
                try {
                    hookEntry.patchDestination(patchCounter);
                    logger.info("Patched: {}", hookEntry);
                } catch (InvalidHookEntryException e) {
                    logger.error("Failed to patch entry {}", hookEntry);
                }
                patchCounter++;
            }
        });

    }

    /**
     * Create or restore a backup of the original Minecraft source file.
     *
     * @param dstClassName the Minecraft class name that should be backed up.
     */
    private void createOrRestoreBackup(String dstClassName) {
        try {
            File sourceFile = HelperUtil.getFileForClass(dstClassName);
            File backupFile = new File(sourceFile.getAbsolutePath() + ".origbak");

            if (sourceFile.exists()) {
                // Check if we already backed up the original file, if so, restore it for patching
                if (backupFile.exists()) {
                    Files.copy(backupFile.toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else { // create a new backup
                    Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main entry point for the hook injector.
     *
     * @param args program arguments.
     */
    public static void main(String[] args) {
        try {
            HookInjector injector = new HookInjector(System.getProperty("user.dir") + "/src/main/resources/patch_map.json");


            System.out.println("=====================");
            System.out.println("Starting injection...");

            injector.injectHooks();

            System.out.println("=====================");
            System.out.println("Injection completed!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
