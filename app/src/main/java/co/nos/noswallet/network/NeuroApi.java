package co.nos.noswallet.network;

import co.nos.noswallet.network.model.request.GetBlocksInfoRequest;
import co.nos.noswallet.network.nosModel.GetBlocksInfoResponse;
import co.nos.noswallet.network.nosModel.GetPendingBlocksRequest;
import co.nos.noswallet.network.nosModel.GetPendingBlocksResponse;
import co.nos.noswallet.network.nosModel.LoginRequest;
import co.nos.noswallet.network.nosModel.LoginResponse;
import co.nos.noswallet.network.nosModel.NeuroHistoryRequest;
import co.nos.noswallet.network.nosModel.NeuroHistoryResponse;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NeuroApi {

    @POST("/")
    Observable<NeuroHistoryResponse> getAccountHistory(@Body NeuroHistoryRequest request);

    @POST("/")
    Observable<GetPendingBlocksResponse> getPendingBlocks(@Body GetPendingBlocksRequest request);

    @POST("/")
    Observable<Response<ResponseBody>> getBlocksInfo(@Body GetBlocksInfoRequest request);

    @POST("/")
    Observable<LoginResponse> login(@Body LoginRequest request);

}
