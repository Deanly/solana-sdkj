package net.deanly.solana.sdk.rpc.client.websocket;

import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.rpc.types.Commitment;
import net.deanly.solana.sdk.rpc.response.ResValueProgram;

import java.util.List;

public interface WebsocketMethodApi {

    // https://solana.com/ko/docs/rpc/websocket/accountsubscribe
    RpcResponse<Long> accountSubscribe(String accountKey, Commitment commitment, String encoding, NotificationListener<RpcNotificationV2<ResValueAccountInfo>> listener);

    // https://solana.com/ko/docs/rpc/websocket/accountunsubscribe
    RpcResponse<Boolean> accountUnsubscribe(Long subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/blocksubscribe
    RpcResponse<Long> blockSubscribe(Commitment commitment, String encoding, NotificationListener<RpcNotificationV2<ResValueBlock>> listener);

    // https://solana.com/ko/docs/rpc/websocket/blockunsubscribe
    RpcResponse<Boolean> blockUnsubscribe(Long subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/logssubscribe
    RpcResponse<Long> logsSubscribe(List<String> mention, Commitment commitment, NotificationListener<RpcNotificationV2<ResValueLog>> listener);

    // https://solana.com/ko/docs/rpc/websocket/logsunsubscribe
    RpcResponse<Boolean> logsUnsubscribe(Long subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/programsubscribe
    RpcResponse<Long> programSubscribe(String programId, Commitment commitment, String encoding, NotificationListener<RpcNotificationV2<ResValueProgram>> listener);

    // https://solana.com/ko/docs/rpc/websocket/programunsubscribe
    RpcResponse<Boolean> programUnsubscribe(Long subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/rootsubscribe
    RpcResponse<Long> rootSubscribe(NotificationListener<RpcNotification<Long>> listener);

    // https://solana.com/ko/docs/rpc/websocket/rootunsubscribe
    RpcResponse<Boolean> rootUnsubscribe(Long subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/signaturesubscribe
    RpcResponse<Long> signatureSubscribe(String signature, NotificationListener<RpcNotificationV2<String>> listener);

    // https://solana.com/ko/docs/rpc/websocket/signatureunsubscribe
    RpcResponse<Boolean> signatureUnsubscribe(Long subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/slotsubscribe
    RpcResponse<Long> slotSubscribe(NotificationListener<RpcNotification<ResValueSlot>> listener);

    // https://solana.com/ko/docs/rpc/websocket/slotunsubscribe
    RpcResponse<Boolean> slotUnsubscribe(Long subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/slotsupdatessubscribe
    RpcResponse<Long> slotsUpdatesSubscribe(NotificationListener<RpcNotification<ResValueSlotUpdates>> listener);

    // https://solana.com/ko/docs/rpc/websocket/slotsupdatesunsubscribe
    RpcResponse<Boolean> slotsUpdatesUnsubscribe(Long subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/votesubscribe
    RpcResponse<Long> voteSubscribe(NotificationListener<RpcNotification<ResValueVote>> listener);

    // https://solana.com/ko/docs/rpc/websocket/voteunsubscribe
    RpcResponse<Boolean> voteUnsubscribe(Long subscriptionId);
}