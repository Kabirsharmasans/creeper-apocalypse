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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
        long totalCreeperDeaths,
        int highestDayReached
    ) implements CustomPayload {
        public static final Id<SyncChallengeDataPayload> ID = new Id<>(SYNC_CHALLENGE_DATA_ID);

        public static final PacketCodec<RegistryByteBuf, SyncChallengeDataPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SyncChallengeDataPayload::currentDay,
            PacketCodecs.FLOAT, SyncChallengeDataPayload::spawnMultiplier,
            PacketCodecs.VAR_LONG, SyncChallengeDataPayload::totalCreepersSpawned,
            PacketCodecs.VAR_LONG, SyncChallengeDataPayload::totalCreepersKilled,
            PacketCodecs.VAR_LONG, SyncChallengeDataPayload::totalExplosions,
            PacketCodecs.VAR_LONG, SyncChallengeDataPayload::totalCreeperDeaths,
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
                boolean isOp = isOperatorCompat(player, context.server());
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
                CreeperApocalypse.CHALLENGE_DATA.setTotalCreeperDeaths(payload.totalCreeperDeaths());

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
            data.getTotalCreeperDeaths(),
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

    private static boolean isOperatorCompat(ServerPlayerEntity player, net.minecraft.server.MinecraftServer server) {
        if (server == null) {
            return false;
        }

        Object playerManager = server.getPlayerManager();
        List<Object> candidateArgs = new ArrayList<>();
        candidateArgs.add(player);

        Object gameProfile = null;
        try {
            gameProfile = player.getGameProfile();
            if (gameProfile != null) {
                candidateArgs.add(gameProfile);
            }
        } catch (Throwable ignored) {
            // Keep compatibility if profile API shifts.
        }

        if (gameProfile != null) {
            try {
                Method getOpListMethod = playerManager.getClass().getMethod("getOpList");
                Object opList = getOpListMethod.invoke(playerManager);
                if (opList != null) {
                    for (Method opListMethod : opList.getClass().getMethods()) {
                        if (!opListMethod.getName().equals("get") || opListMethod.getParameterCount() != 1) {
                            continue;
                        }

                        Class<?> parameterType = opListMethod.getParameterTypes()[0];
                        if (parameterType.isInstance(gameProfile)) {
                            Object opEntry = opListMethod.invoke(opList, gameProfile);
                            if (opEntry != null) {
                                candidateArgs.add(opEntry);
                            }
                            break;
                        }
                    }
                }
            } catch (ReflectiveOperationException ignored) {
                // Not all versions expose op list APIs the same way.
            }
        }

        for (Method method : playerManager.getClass().getMethods()) {
            if (!method.getName().equals("isOperator") || method.getParameterCount() != 1) {
                continue;
            }

            Class<?> parameterType = method.getParameterTypes()[0];
            for (Object candidateArg : candidateArgs) {
                if (candidateArg != null && parameterType.isInstance(candidateArg)) {
                    try {
                        Object result = method.invoke(playerManager, candidateArg);
                        if (result instanceof Boolean boolResult) {
                            return boolResult;
                        }
                    } catch (ReflectiveOperationException ignored) {
                        // Try the next overload/candidate.
                    }
                }
            }
        }

        return false;
    }
}

