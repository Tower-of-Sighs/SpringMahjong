package com.sighs.springmahjong.client;

import com.sighs.springmahjong.Springmahjong;
import com.sighs.springmahjong.client.render.MahjongTableRenderer;
import com.sighs.springmahjong.client.render.SeatEntityRenderer;
import com.sighs.springmahjong.entity.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Springmahjong.TABLE_ENTITY.get(),
            MahjongTableRenderer::new);
        event.registerEntityRenderer(ModEntities.SEAT.get(), SeatEntityRenderer::new);
    }
}
