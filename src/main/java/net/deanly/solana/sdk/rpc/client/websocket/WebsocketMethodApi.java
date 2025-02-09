package net.deanly.solana.sdk.rpc.client.websocket;

import com.google.common.primitives.UnsignedLong;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.rpc.request.config.*;
import net.deanly.solana.sdk.rpc.request.filter.BlockFilter;
import net.deanly.solana.sdk.rpc.request.filter.LogsFilter;
import net.deanly.solana.sdk.rpc.response.*;
import net.deanly.solana.sdk.rpc.response.ResValueProgram;
import net.deanly.solana.sdk.types.Signature;
import net.deanly.solana.sdk.types.SubscriptionId;

public interface WebsocketMethodApi {

    // https://solana.com/ko/docs/rpc/websocket/accountsubscribe
    RpcResponse<SubscriptionId> accountSubscribe(PublicKey accountKey, AccountSubscriptionConfig config,
                                                 NotificationListener<RpcNotificationV2<NotiValueAccountInfo>> listener);

    // https://solana.com/ko/docs/rpc/websocket/accountunsubscribe
    RpcResponse<Boolean> accountUnsubscribe(SubscriptionId subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/blocksubscribe
    RpcResponse<SubscriptionId> blockSubscribe(BlockFilter filter, BlockConfig2 config,
                                               NotificationListener<RpcNotificationV2<NotiValueBlock>> listener);

    // https://solana.com/ko/docs/rpc/websocket/blockunsubscribe
    RpcResponse<Boolean> blockUnsubscribe(SubscriptionId subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/logssubscribe
    RpcResponse<SubscriptionId> logsSubscribe(LogsFilter filter, LogsConfig config, NotificationListener<RpcNotificationV2<NotiValueLog>> listener);

    // https://solana.com/ko/docs/rpc/websocket/logsunsubscribe
    RpcResponse<Boolean> logsUnsubscribe(SubscriptionId subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/programsubscribe
    RpcResponse<SubscriptionId> programSubscribe(PublicKey programId, ProgramConfig config, NotificationListener<RpcNotificationV2<NotiValueProgram>> listener);

    // https://solana.com/ko/docs/rpc/websocket/programunsubscribe
    RpcResponse<Boolean> programUnsubscribe(SubscriptionId subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/rootsubscribe
    RpcResponse<SubscriptionId> rootSubscribe(NotificationListener<RpcNotification<UnsignedLong>> listener);

    // https://solana.com/ko/docs/rpc/websocket/rootunsubscribe
    RpcResponse<Boolean> rootUnsubscribe(SubscriptionId subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/signaturesubscribe
    RpcResponse<SubscriptionId> signatureSubscribe(Signature signature, SignatureConfig config, NotificationListener<RpcNotificationV2<NotiValueSignature>> listener);

    // https://solana.com/ko/docs/rpc/websocket/signatureunsubscribe
    RpcResponse<Boolean> signatureUnsubscribe(SubscriptionId subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/slotsubscribe
    RpcResponse<SubscriptionId> slotSubscribe(NotificationListener<RpcNotification<NotiValueSlot>> listener);

    // https://solana.com/ko/docs/rpc/websocket/slotunsubscribe
    RpcResponse<Boolean> slotUnsubscribe(SubscriptionId subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/slotsupdatessubscribe
    RpcResponse<SubscriptionId> slotsUpdatesSubscribe(NotificationListener<RpcNotification<NotiValueSlotUpdates>> listener);

    // https://solana.com/ko/docs/rpc/websocket/slotsupdatesunsubscribe
    RpcResponse<Boolean> slotsUpdatesUnsubscribe(SubscriptionId subscriptionId);

    // https://solana.com/ko/docs/rpc/websocket/votesubscribe
    RpcResponse<SubscriptionId> voteSubscribe(NotificationListener<RpcNotification<NotiValueVote>> listener);

    // https://solana.com/ko/docs/rpc/websocket/voteunsubscribe
    RpcResponse<Boolean> voteUnsubscribe(SubscriptionId subscriptionId);
}