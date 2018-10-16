package co.nos.noswallet.base;

public interface HasComponent<Component> {

    Component getComponent(Class<Component> klazz);

    boolean hasComponent(Class<Component> klazz);

}
