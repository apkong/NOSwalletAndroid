package co.nos.noswallet.db;

public interface RepresentativesProvider {

    String provideRepresentative();

    default void setRepresentative(String representative) {
    }
}
