package com.creeperapocalypse.network;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.data.ChallengeData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Handles all client-server networking for the mod
 */
public class ModNetworking {

    public static final Identifier SYNC_CHALLENGE_DATA_ID = Identifier.of(CreeperApocalypse.MOD_ID, "sync_challenge");
    public static final Identifier RESET_REQUEST_ID = Identifier.of(CreeperApocalypse.MOD_ID, "reset_request");
    public static final Identifier STATS_REQUEST_ID = Identifier.of(CreeperApocalypse.MOD_ID, "stats_request");

    public record SyncChallengeDataPayload(
        int currentDay,
        float spawnMultiplier,
        long totalCreepersSpawned,
        long totalCreepersKilled,
        long totalExplosions,
        int highestDayReached
    ) implements CustomPayload {
        public static final Id<SyncChallengeDataPayload> ID = new Id<>(SYNC_CHALLENGE_DATA_ID);

        public static final PacketCodec<RegistryByteBuf, SyncChallengeDataPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SyncChallengeDataPayload::currentDay,
            PacketCodecs.FLOAT, SyncChallengeDataPayload::spawnMultiplier,
            PacketCodecs.VAR_LONG, SyncChallengeDataPayload::totalCreepersSpawned,
            PacketCodecs.VAR_LONG, SyncChallengeDataPayload::totalCreepersKilled,
            PacketCodecs.VAR_LONG, SyncChallengeDataPayload::totalExplosions,
            PacketCodecs.INTEGER, SyncChallengeDataPayload::highestDayReached,
            SyncChallengeDataPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record ResetRequestPayload() implements CustomPayload {
        public static final Id<ResetRequestPayload> ID = new Id<>(RESET_REQUEST_ID);
        public static final PacketCodec<RegistryByteBuf, ResetRequestPayload> CODEC = PacketCodec.unit(new ResetRequestPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record StatsRequestPayload() implements CustomPayload {
        public static final Id<StatsRequestPayload> ID = new Id<>(STATS_REQUEST_ID);
        public static final PacketCodec<RegistryByteBuf, StatsRequestPayload> CODEC = PacketCodec.unit(new StatsRequestPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void registerRuntimeControlPackets() {
        PayloadTypeRegistry.playC2S().register(ResetRequestPayload.ID, ResetRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StatsRequestPayload.ID, StatsRequestPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ResetRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                boolean isOp = context.server().getPlayerManager().isOperator(player.getGameProfile());
                if (isOp || context.server().isSingleplayer()) {
                    CreeperApocalypse.resetChallenge();
                    CreeperApocalypse.CHALLENGE_DATA.save(context.server());
                    com.creeperapocalypse.data.PlayerStats.saveAll(context.server());

                    for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
                        syncChallengeData(p, CreeperApocalypse.CHALLENGE_DATA);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(StatsRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                syncChallengeData(context.player(), CreeperApocalypse.CHALLENGE_DATA);
            });
        });
    }

    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(SyncChallengeDataPayload.ID, SyncChallengeDataPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(SyncChallengeDataPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                CreeperApocalypse.CHALLENGE_DATA.setCurrentDay(payload.currentDay());
                CreeperApocalypse.CHALLENGE_DATA.setSpawnMultiplier(payload.spawnMultiplier());
                CreeperApocalypse.CHALLENGE_DATA.setHighestDayReached(payload.highestDayReached());
                CreeperApocalypse.CHALLENGE_DATA.setTotalCreepersSpawned(payload.totalCreepersSpawned());
                CreeperApocalypse.CHALLENGE_DATA.setTotalCreepersKilled(payload.totalCreepersKilled());
                CreeperApocalypse.CHALLENGE_DATA.setTotalExplosions(payload.totalExplosions());

                CreeperApocalypse.LOGGER.debug("Received challenge sync: Day " + payload.currentDay());
            });
        });
    }

    public static void syncChallengeData(ServerPlayerEntity player, ChallengeData data) {
        SyncChallengeDataPayload payload = new SyncChallengeDataPayload(
            data.getCurrentDay(),
            data.getSpawnMultiplier(),
            data.getTotalCreepersSpawned(),
            data.getTotalCreepersKilled(),
            data.getTotalExplosions(),
            data.getHighestDayReached()
        );

        ServerPlayNetworking.send(player, payload);
    }

    public static void syncToAllPlayers(net.minecraft.server.MinecraftServer server, ChallengeData data) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            syncChallengeData(player, data);
        }
    }

    public static void sendResetRequest() {
        ClientPlayNetworking.send(new ResetRequestPayload());
    }

    public static void sendStatsRequest() {
        ClientPlayNetworking.send(new StatsRequestPayload());
    }
}

