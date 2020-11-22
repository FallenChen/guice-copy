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

    public void testCircularlyDependentConstructors(){
        ContainerBuilder builder = new ContainerBuilder();
        builder
                .factory(A.class, AImpl.class)
                .factory(B.class, BImpl.class);
        Container container = builder.create(false);
        A a = container.inject(AImpl.class);
        assertNotNull(a.getB().getA());
    }

    interface A {
        B getB();
    }

    interface B {
        A getA();
    }

    static class AImpl implements A {
        final B b;

        @Inject
        public AImpl(B b){
            this.b = b;
        }

        @Override
        public B getB() {
            return b;
        }
    }

    static class BImpl implements B {
        final A a;

        @Inject
        public BImpl(A a) {
            this.a = a;
        }

        @Override
        public A getA() {
            return a;
        }
    }

    public void testInjectStatics() {
        new ContainerBuilder()
                .constant("s", "test")
                .constant("i", 5)
                .injectStatics(Static.class)
                .create(false);

        assertEquals("test", Static.s);
        assertEquals(5, Static.i);
    }

    static class Static {

        @Inject("i")
        static int i;

        static String s;

        @Inject("s")
        static void setS(String s){
            Static.s = s;
        }

    }
}
