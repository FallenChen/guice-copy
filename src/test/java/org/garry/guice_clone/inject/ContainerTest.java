package org.garry.guice_clone.inject;

import junit.framework.TestCase;
import org.garry.gucie_clone.inject.*;

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

        ContainerBuilder builder = new ContainerBuilder();
        builder
                .factory(Bar.class, BarImpl.class)
                .factory(Tee.class, TeeImpl.class)
                .constant("s","test")
                .constant("i",5);
        return builder.create(false);

    }


    public void testGetInstance(){
        Container container = createFooContainer();

        Bar bar = container.getInstance(Bar.class, Container.DEFAULT_NAME);
        assertEquals("test",bar.getTee().getS());
        assertEquals(5, bar.getI());
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

    @Scoped(Scope.SINGLETON)
    static class BarImpl implements Bar {

        @Inject("i")
        int i;

        Tee tee;

        @Inject
        void initialize(Tee tee){
            this.tee = tee;
        }

        @Override
        public Tee getTee() {
            return tee;
        }

        @Override
        public int getI() {
            return i;
        }
    }

    static class TeeImpl implements Tee {

        final String s;

        @Inject
        Bar bar;

        @Inject
        TeeImpl(@Inject("s") String s){
            this.s = s;
        }

        @Override
        public String getS() {
            return s;
        }

        @Override
        public Bar getBar() {
            return bar;
        }
    }
}
