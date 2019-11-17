package at.ac.uibk.chaas.minesteg.hooks;

import java.util.Random;

/**
 * A Testclass that will be used to jUnit test the HookInjector.
 */
public class HookEntryTestForPatching {
    private int var1 = 666;

    public void testMethod1(int i) {
        System.out.println(i + " = i");
        // Insertion point here:
        // Insertion point end
    }

    public void testMethod2() {
        if(System.currentTimeMillis() > 1000) {
            System.out.println("> 1000");
        } else {
            System.out.println("< 1000");
        }

        // Insertion point here:
        // Insertion point end

        testMethod1(1);
        testMethod1(2);
    }

    private int testMethod3(int i) {
        if(i > 0) {
            return testMethod3(i -1);
        }

        testMethod2();
        // Insertion point here:
        // Insertion point end
        testMethod1(i);

        return 0;
    }

    public void testMethod4() {

        int iVar1 = new Random().nextInt();
        int cVar1 = new Random().nextInt(), aVar2 = new Random().nextInt();

        if(iVar1 == 1) {
            if(cVar1 < 5) {
                testMethod3(aVar2);
            } else {
                testMethod1(cVar1);
            }
        }

        if(cVar1 > 0) {
            System.out.println("Hello: " + var1);
            // Insertion point here:
            // Insertion point end
        }
    }
}
