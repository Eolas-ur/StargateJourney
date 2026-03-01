package net.povstalec.sgjourney.common.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.povstalec.sgjourney.common.block_entities.transporter.RingPanelEntity;
import net.povstalec.sgjourney.StargateJourney;

public record ServerboundRingPanelUpdatePacket(BlockPos blockPos, int number) implements CustomPacketPayload
{
        public static final CustomPacketPayload.Type<ServerboundRingPanelUpdatePacket> TYPE =
                        new CustomPacketPayload.Type<>(StargateJourney.sgjourneyLocation("c2s_ring_panel_update"));
        
        public static final StreamCodec<ByteBuf, ServerboundRingPanelUpdatePacket> STREAM_CODEC = StreamCodec.composite(
                        BlockPos.STREAM_CODEC, ServerboundRingPanelUpdatePacket::blockPos,
                        ByteBufCodecs.VAR_INT, ServerboundRingPanelUpdatePacket::number,
                        ServerboundRingPanelUpdatePacket::new
        );
        
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
        {
                return TYPE;
        }

    public static void handle(ServerboundRingPanelUpdatePacket packet, IPayloadContext ctx)
    {
                ctx.enqueueWork(() -> {
                        if(ctx.player().distanceToSqr(packet.blockPos.getX() + 0.5, packet.blockPos.getY() + 0.5, packet.blockPos.getZ() + 0.5) > 64.0)
                                return;

                        final BlockEntity blockEntity = ctx.player().level().getBlockEntity(packet.blockPos);
                        
                        if(blockEntity instanceof RingPanelEntity ringPanel)
                                ringPanel.activateRings(packet.number);
                });
    }
}


