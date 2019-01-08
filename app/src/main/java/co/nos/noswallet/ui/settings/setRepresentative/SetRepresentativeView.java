package co.nos.noswallet.ui.settings.setRepresentative;

public interface SetRepresentativeView {

    void clearRepresentativeError();

    void showRepresentativeError(int stringRes);

    void showRepresentativeSavedAndExit(int stringRes);

    void onRepresentativeReceived(String previousRepresentative);
}
