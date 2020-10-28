package org.garry.gucie_clone.inject;

import java.lang.reflect.Member;
import java.util.LinkedHashMap;

/**
 * An immutable snapshot of the current context which is safe to
 * expose to client code
 * @param <T>
 */
class ExternalContext<T> implements Context {

    final Member member;
    final Key<T> key;
    final ContainerImpl container;


    public ExternalContext(Member member, Key<T> key, ContainerImpl container) {
        this.member = member;
        this.key = key;
        this.container = container;
    }

    public Class<T> getType(){
        return key.getType();
    }

    public Scope.Strategy getScopeStrategy(){
        return container.localScopeStrategy.get();
    }

    @Override
    public ContainerImpl getContainer() {
        return container;
    }

    @Override
    public Member getMember() {
        return member;
    }

    public String getName(){
        return key.getName();
    }

    public String toString(){
        return "Context" + new LinkedHashMap<String,Object>(){{
            put("member", member);
            put("type",getType());
            put("container",container);
        }}.toString();
    }

    // todo 为什么构造方法是public
    static <T> ExternalContext<T> newInstance(Member member, Key<T> key,
                                              ContainerImpl container){
        return new ExternalContext<>(member,key,container);
    }
}
