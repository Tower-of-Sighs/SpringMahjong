package com.sighs.springmahjong.client.render;

import com.sighs.springmahjong.entity.SeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Invisible renderer for the chair mount entity.
 */
public class SeatEntityRenderer extends EntityRenderer<SeatEntity, EntityRenderState> {
    public SeatEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
        this.shadowStrength = 0.0f;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
