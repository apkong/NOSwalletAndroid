package co.nos.noswallet.network;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import co.nos.noswallet.network.exception.ErrorDispatcher;
import co.nos.noswallet.network.nosModel.NeuroHistoryRequest;
import co.nos.noswallet.network.nosModel.NeuroHistoryResponse;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NeuroClient implements NeuroApi {

    private final NeuroApi api;

    public NeuroClient(String endpoint, Class<NeuroApi> klazz, ErrorDispatcher errorDispatcher) {
        api = new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(buildClient(120))
                .build()
                .create(klazz);
    }

    private OkHttpClient buildClient(final long timeoutInSeconds) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .writeTimeout(timeoutInSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutInSeconds, TimeUnit.SECONDS)
                .connectTimeout(timeoutInSeconds, TimeUnit.SECONDS);

        builder.addNetworkInterceptor(loggingInterceptor);
        return builder.build();
    }

    @Override
    public Observable<NeuroHistoryResponse> getAccountHistory(NeuroHistoryRequest request) {
        return api.getAccountHistory(request);
    }
}
