package at.ac.uibk.chaas.minesteg.hooks;

import java.util.Comparator;

/**
 * A comparator implementation for hook entries.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class HookEntryComparator implements Comparator<HookEntry> {
    @Override
    public int compare(HookEntry o1, HookEntry o2) {
        int o1Line = o1.getDstMethodEndLine() + o1.getDestinationLineNumber();
        int o2Line = o2.getDstMethodEndLine() + o2.getDestinationLineNumber();
        return Integer.compare(o1Line, o2Line);
    }
}
