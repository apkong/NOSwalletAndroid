package co.nano.nanowallet.di.activity;

import co.nano.nanowallet.model.NanoWalletTest;
import co.nos.noswallet.di.activity.ActivityComponent;
import co.nos.noswallet.di.activity.ActivityModule;
import co.nos.noswallet.di.activity.ActivityScope;
import co.nos.noswallet.di.application.ApplicationComponent;
import dagger.Component;

@Component(modules = {ActivityModule.class}, dependencies = {ApplicationComponent.class})
@ActivityScope
public interface TestActivityComponent extends ActivityComponent {
    void inject(NanoWalletTest nanoWalletTest);
}
