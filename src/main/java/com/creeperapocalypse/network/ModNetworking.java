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
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Handles all client-server networking for the mod
 */
public class ModNetworking {
    
    public static final Identifier SYNC_CHALLENGE_DATA_ID = Identifier.of(CreeperApocalypse.MOD_ID, "sync_challenge");
    public static final Identifier CONFIG_UPDATE_ID = Identifier.of(CreeperApocalypse.MOD_ID, "config_update");
    public static final Identifier RESET_REQUEST_ID = Identifier.of(CreeperApocalypse.MOD_ID, "reset_request");
    public static final Identifier STATS_REQUEST_ID = Identifier.of(CreeperApocalypse.MOD_ID, "stats_request");
    
    public record SyncChallengeDataPayload(
        int currentDay,
        float spawnMultiplier,
        long totalCreepersSpawned,
        long totalCreepersKilled,
        long totalExplosions,
        long totalCreeperDeaths,
        int highestDayReached,
        boolean bloodMoonActive,
        boolean chargedDayActive,
        boolean swarmActive
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
            PacketCodecs.BOOLEAN, SyncChallengeDataPayload::bloodMoonActive,
            PacketCodecs.BOOLEAN, SyncChallengeDataPayload::chargedDayActive,
            PacketCodecs.BOOLEAN, SyncChallengeDataPayload::swarmActive,
            SyncChallengeDataPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record ConfigUpdatePayload(
        boolean enabled,
        float maxMultiplier,
        float escalationSpeed
    ) implements CustomPayload {
        public static final Id<ConfigUpdatePayload> ID = new Id<>(CONFIG_UPDATE_ID);
        
        public static final PacketCodec<RegistryByteBuf, ConfigUpdatePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, ConfigUpdatePayload::enabled,
            PacketCodecs.FLOAT, ConfigUpdatePayload::maxMultiplier,
            PacketCodecs.FLOAT, ConfigUpdatePayload::escalationSpeed,
            ConfigUpdatePayload::new
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
    
    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(ConfigUpdatePayload.ID, ConfigUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ResetRequestPayload.ID, ResetRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StatsRequestPayload.ID, StatsRequestPayload.CODEC);
        
        ServerPlayNetworking.registerGlobalReceiver(ConfigUpdatePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                // Check if player has operator permissions by wrapping GameProfile in PlayerConfigEntry
                PlayerConfigEntry entry = new PlayerConfigEntry(player.getGameProfile());
                boolean isOp = context.server().getPlayerManager().getOpList().get(entry) != null;
                if (isOp || context.server().isSingleplayer()) {
                    CreeperApocalypse.CONFIG.setEnabled(payload.enabled());
                    CreeperApocalypse.CONFIG.setMaxSpawnMultiplier(payload.maxMultiplier());
                    CreeperApocalypse.CONFIG.setEscalationSpeed(payload.escalationSpeed());
                    CreeperApocalypse.CONFIG.save();
                    
                    CreeperApocalypse.LOGGER.info("Config updated by " + player.getName().getString());
                    
                    for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
                        syncChallengeData(p, CreeperApocalypse.CHALLENGE_DATA);
                    }
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(ResetRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                PlayerConfigEntry entry = new PlayerConfigEntry(player.getGameProfile());
                boolean isOp = context.server().getPlayerManager().getOpList().get(entry) != null;
                if (isOp || context.server().isSingleplayer()) {
                    CreeperApocalypse.resetChallenge();
                    
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
        var milestoneHandler = CreeperApocalypse.getMilestoneHandler();
        
        SyncChallengeDataPayload payload = new SyncChallengeDataPayload(
            data.getCurrentDay(),
            data.getSpawnMultiplier(),
            data.getTotalCreepersSpawned(),
            data.getTotalCreepersKilled(),
            data.getTotalExplosions(),
            data.getTotalCreeperDeaths(),
            data.getHighestDayReached(),
            milestoneHandler.isBloodMoonActive(),
            milestoneHandler.isChargedDayActive(),
            milestoneHandler.isSwarmActive()
        );
        
        ServerPlayNetworking.send(player, payload);
    }
    
    public static void syncToAllPlayers(net.minecraft.server.MinecraftServer server, ChallengeData data) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            syncChallengeData(player, data);
        }
    }
    
    public static void sendConfigUpdate(boolean enabled, float maxMultiplier, float escalationSpeed) {
        ClientPlayNetworking.send(new ConfigUpdatePayload(enabled, maxMultiplier, escalationSpeed));
    }
    
    public static void sendResetRequest() {
        ClientPlayNetworking.send(new ResetRequestPayload());
    }
    
    public static void sendStatsRequest() {
        ClientPlayNetworking.send(new StatsRequestPayload());
    }
}
