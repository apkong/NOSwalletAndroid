package co.nos.noswallet.network;

import co.nos.noswallet.network.nosModel.NeuroHistoryRequest;
import co.nos.noswallet.network.nosModel.NeuroHistoryResponse;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NeuroApi {

    @POST("/")
    Observable<NeuroHistoryResponse> getAccountHistory(@Body NeuroHistoryRequest request);

}
