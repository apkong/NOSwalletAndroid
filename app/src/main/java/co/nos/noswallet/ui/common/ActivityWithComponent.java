package co.nos.noswallet.ui.common;

import co.nos.noswallet.di.activity.ActivityComponent;
import co.nos.noswallet.di.application.ApplicationComponent;

/**
 * Interface for Activity with a Component
 */

public interface ActivityWithComponent {
    ActivityComponent getActivityComponent();
    ApplicationComponent getApplicationComponent();
}
