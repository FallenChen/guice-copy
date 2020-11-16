package org.garry.guice_clone.inject;

import junit.framework.TestCase;
import org.garry.gucie_clone.inject.Container;
import org.garry.gucie_clone.inject.Inject;

public class ContainerTest extends TestCase {

    public void testInjection() {

        Container container = createFooContainer();

        Foo foo = container.inject(Foo.class);

        assertEquals("test", foo.s);
        assertEquals("test",foo.bar.getTee().getS());
        assertSame(foo.bar, foo.copy);
        assertEquals(5, foo.i);
        assertEquals(5, foo.bar.getI());

        // Test circular dependency
        assertSame(foo.bar, foo.bar.getTee().getBar());
    }

    private Container createFooContainer() {

    }


    static class Foo {

        @Inject Bar bar;
        @Inject Bar copy;

        @Inject("s") String s;

        int i;

        @Inject("i")
        void setI(int i){
            this.i = i;
        }


    }

    interface Bar {

        Tee getTee();

        int getI();
    }

    interface Tee {

        String getS();

        Bar getBar();
    }
}
