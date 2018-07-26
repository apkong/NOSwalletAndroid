package co.nos.noswallet.network.interactor;

import java.math.BigInteger;

import javax.inject.Inject;

import co.nos.noswallet.model.Address;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.model.request.GetBlocksInfoRequest;
import co.nos.noswallet.network.nosModel.WorkRequest;
import io.reactivex.Observable;
import io.reactivex.annotations.Experimental;
import io.realm.Realm;

public class SendCoinsUseCase {

    private final NeuroClient api;
    private final Realm realm;

    @Inject
    public SendCoinsUseCase(NeuroClient api, Realm realm) {
        this.api = api;
        this.realm = realm;
    }

    @Experimental
    public Observable<Object> sendCoins(String previous, Address destination, BigInteger balance) {
        // create a work block
        //todo:
        return api.generateWork(new WorkRequest(previous))
                .flatMap(workResponse -> {
                    return api.getBlocksInfo(new GetBlocksInfoRequest(previous))
                            .flatMap(blocksResponse -> {
                                return Observable.empty();
                            });
                });

//        requestQueue.add(new RequestItem<>(new WorkRequest(previous)));

        // create a get_block request
//        requestQueue.add(new RequestItem<>(new GetBlocksInfoRequest(new String[]{previous})));

        // create a state block for sending
//        requestQueue.add(new RequestItem<>(new StateBlock(
//                BlockTypes.SEND,
//                private_key,
//                previous,
//                wallet.getRepresentative(),
//                balance.toString(),
//                destination.getAddress()
//        )));
    }
}
