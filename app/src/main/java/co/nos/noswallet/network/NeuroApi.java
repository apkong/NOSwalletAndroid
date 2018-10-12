package co.nos.noswallet.network;

import co.nos.noswallet.network.model.request.GetBlocksInfoRequest;
import co.nos.noswallet.network.nosModel.AccountInfoRequest;
import co.nos.noswallet.network.nosModel.AccountInfoResponse;
import co.nos.noswallet.network.nosModel.GetBlocksInfoResponse;
import co.nos.noswallet.network.nosModel.GetPendingBlocksRequest;
import co.nos.noswallet.network.nosModel.GetPendingBlocksResponse;
import co.nos.noswallet.network.nosModel.NeuroHistoryRequest;
import co.nos.noswallet.network.nosModel.NeuroHistoryResponse;
import co.nos.noswallet.network.nosModel.ProcessRequest;
import co.nos.noswallet.network.nosModel.ProcessResponse;
import co.nos.noswallet.network.nosModel.WorkRequest;
import co.nos.noswallet.network.nosModel.WorkResponse;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NeuroApi {

    @POST("/")
    Observable<NeuroHistoryResponse> getAccountHistory(@Body NeuroHistoryRequest request);

    @POST("/")
    Observable<GetPendingBlocksResponse> getPendingBlocks(@Body GetPendingBlocksRequest request);

    @POST("/")
    Observable<GetBlocksInfoResponse> getBlocksInfo(@Body GetBlocksInfoRequest request);

    @POST("/")
    Observable<AccountInfoResponse> getAccountInfo(@Body AccountInfoRequest request);

    @POST("/")
    Observable<WorkResponse> generateWork(@Body WorkRequest request);

    @POST("/")
    Observable<ProcessResponse> process(@Body ProcessRequest request);

}
