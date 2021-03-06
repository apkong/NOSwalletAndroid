package co.nos.noswallet.base;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

abstract public class BasePresenter<View extends BaseView> {


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    protected View view;

    public void attachView(View view) {
        this.view = view;
    }

    public void onDestroy(){
        compositeDisposable.clear();
    }

    public void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }
}
