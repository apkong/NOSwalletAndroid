package co.nos.noswallet.network.websockets.model;

public enum WebSocketsState {
    IDLE,
    GET_ACCOUNT_HISTORY,
    GET_PENDING_BLOCKS,
    GET_ACCOUNT_INFO,
    GENERATE_WORK,
    PROCESS_WORK,
    TRANSFER_COINS_GENERATE_WORK,
    TRANSFER_COINS_PROCESS_WORK,

}
