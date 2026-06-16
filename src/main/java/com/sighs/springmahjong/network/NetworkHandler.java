package com.sighs.springmahjong.network;

import com.sighs.springmahjong.Springmahjong;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Springmahjong.MODID)
public class NetworkHandler {
    public static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // C2S packets
        registrar.playToServer(
            CJoinTable.TYPE, CJoinTable.STREAM_CODEC, ServerPacketHandler::handleJoinTable);
        registrar.playToServer(
            CLeaveTable.TYPE, CLeaveTable.STREAM_CODEC, ServerPacketHandler::handleLeaveTable);
        registrar.playToServer(
            CSitDown.TYPE, CSitDown.STREAM_CODEC, ServerPacketHandler::handleSitDown);
        registrar.playToServer(
            CStandUp.TYPE, CStandUp.STREAM_CODEC, ServerPacketHandler::handleStandUp);
        registrar.playToServer(
            CPlayerReady.TYPE, CPlayerReady.STREAM_CODEC, ServerPacketHandler::handlePlayerReady);
        registrar.playToServer(
            CDiscard.TYPE, CDiscard.STREAM_CODEC, ServerPacketHandler::handleDiscard);
        registrar.playToServer(
            CReaction.TYPE, CReaction.STREAM_CODEC, ServerPacketHandler::handleReaction);

        // S2C packets
        registrar.playToClient(
            STableInfo.TYPE, STableInfo.STREAM_CODEC, ClientPacketHandler::handleTableInfo);
        registrar.playToClient(
            SPatronJoined.TYPE, SPatronJoined.STREAM_CODEC, ClientPacketHandler::handlePatronJoined);
        registrar.playToClient(
            SPatronLeft.TYPE, SPatronLeft.STREAM_CODEC, ClientPacketHandler::handlePatronLeft);
        registrar.playToClient(
            SSitDown.TYPE, SSitDown.STREAM_CODEC, ClientPacketHandler::handleSitDown);
        registrar.playToClient(
            SStandUp.TYPE, SStandUp.STREAM_CODEC, ClientPacketHandler::handleStandUp);
        registrar.playToClient(
            SStateSync.TYPE, SStateSync.STREAM_CODEC, ClientPacketHandler::handleStateSync);
        registrar.playToClient(
            SDealHand.TYPE, SDealHand.STREAM_CODEC, ClientPacketHandler::handleDealHand);
        registrar.playToClient(
            SYourDraw.TYPE, SYourDraw.STREAM_CODEC, ClientPacketHandler::handleYourDraw);
        registrar.playToClient(
            SOpponentDraw.TYPE, SOpponentDraw.STREAM_CODEC, ClientPacketHandler::handleOpponentDraw);
        registrar.playToClient(
            SReactionRequest.TYPE, SReactionRequest.STREAM_CODEC, ClientPacketHandler::handleReactionRequest);
        registrar.playToClient(
            SBroadcastTurn.TYPE, SBroadcastTurn.STREAM_CODEC, ClientPacketHandler::handleBroadcastTurn);
        registrar.playToClient(
            SBroadcastDiscard.TYPE, SBroadcastDiscard.STREAM_CODEC, ClientPacketHandler::handleBroadcastDiscard);
        registrar.playToClient(
            SBroadcastMeld.TYPE, SBroadcastMeld.STREAM_CODEC, ClientPacketHandler::handleBroadcastMeld);
        registrar.playToClient(
            SBroadcastSettlement.TYPE, SBroadcastSettlement.STREAM_CODEC, ClientPacketHandler::handleSettlement);
        registrar.playToClient(
            SBroadcastGameEnd.TYPE, SBroadcastGameEnd.STREAM_CODEC, ClientPacketHandler::handleGameEnd);
        registrar.playToClient(
            SError.TYPE, SError.STREAM_CODEC, ClientPacketHandler::handleError);
    }
}

