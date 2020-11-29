package org.garry.guice_clone.inject;

import junit.framework.TestCase;
import org.garry.gucie_clone.inject.ContainerBuilder;
import org.garry.gucie_clone.inject.Context;
import org.garry.gucie_clone.inject.Factory;
import org.garry.gucie_clone.inject.Inject;

import java.lang.reflect.Member;

public class FactoryTest extends TestCase {

    public void testInjection() {
        ContainerBuilder cb = new ContainerBuilder();

        // Called from getInstance
        cb.factory(Foo.class, createFactory(Foo.class, "default", null));
    }

    <T> Factory<T> createFactory(final Class<T> type, final String name, final Member expectedMember) {
        return new Factory<T>() {
            @Override
            public T create(Context context) throws Exception {
                assertEquals(expectedMember, context.getMember());
                assertEquals(name, context.getName());
                assertEquals(type, context.getType());
                return context.getContainer().inject(type);
            }
        };
    }


    static class Foo {

        final Bar bar;

        @Inject("foobar")
        Foo(Bar bar){
            this.bar = bar;
        }
    }

    static class Bar {

        @Inject("tee2")
        Tee tee2;

        final Tee tee1;

        @Inject("tee1") Bar(Tee tee1) {
            this.tee1 = tee1;
        }
    }

    static class Tee {

        Bob bob1, bob2;

        @Inject
        void execute(@Inject("bob1") Bob bob1,
                     @Inject("bob2") Bob bob2){
            this.bob1 = bob1;
            this.bob2 = bob2;
        }
    }

    static class Bob {}
}
