package co.nos.noswallet.di.application;

import android.content.Context;

import java.nio.charset.Charset;

import javax.inject.Singleton;

import co.nos.noswallet.BuildConfig;
import co.nos.noswallet.network.ApiResponseMapper;
import co.nos.noswallet.network.MsgPackCompressor;
import co.nos.noswallet.network.NeuroApi;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.ZlipCompressor;
import co.nos.noswallet.network.exception.ErrorDispatcher;
import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    final Context mContext;

    public ApplicationModule(Context context) {
        mContext = context;
    }

    @Provides
    Context providesApplicationContext() {
        return mContext;
    }

    @Provides
    NeuroClient providesNeuroClient(ErrorDispatcher errorDispatcher) {
        return new NeuroClient(BuildConfig.CONNECTION_URL, NeuroApi.class, errorDispatcher);
    }

    @Provides
    ApiResponseMapper providesApiResponseMapper() {
        Charset charset = Charset.forName("UTF-8");

        MsgPackCompressor msgCompressor = new MsgPackCompressor(charset);
        ZlipCompressor zlipCompressor = new ZlipCompressor(charset);

        return new ApiResponseMapper(msgCompressor, zlipCompressor);
    }

}
