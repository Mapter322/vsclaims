package com.mapter.vsclaims.ship;

import com.mapter.vsclaims.claim.Claim;
import com.mapter.vsclaims.claim.ClaimManager;
import com.mapter.vsclaims.claim.VsClaimManager;
import net.minecraft.server.level.ServerLevel;
import java.util.UUID;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;


@Mod.EventBusSubscriber
public class VSShipEventHandler {

    private static final Logger LOGGER = LogManager.getLogger("vsclaims/VSShipEventHandler");

    // Известные shipId — чтобы не добавлять одно и то же дважды
    private static final java.util.Set<String> knownShipIds =
            java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 200; // каждые 10 секунд (20 tps)


    // Сброс при загрузке мира

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;
        knownShipIds.clear();
        cachedGetAllShips = null;

        // Предзаполняем knownShipIds всеми уже существующими кораблями
        // чтобы первый тик не добавил их все как "новые"
        try {
            Object shipWorld = level.getClass().getMethod("getShipObjectWorld").invoke(level);
            if (shipWorld != null) {
                cachedGetAllShips = shipWorld.getClass().getMethod("getAllShips");
                Iterable<?> ships = (Iterable<?>) cachedGetAllShips.invoke(shipWorld);
                int count = 0;
                for (Object ship : ships) {
                    try {
                        knownShipIds.add(ship.getClass().getMethod("getId").invoke(ship).toString());
                        count++;
                    } catch (Exception ignored) {}
                }
                LOGGER.info("VSShipEventHandler: предзаполнено {} кораблей", count);
            }
        } catch (Exception e) {
            LOGGER.warn("VSShipEventHandler: ошибка предзаполнения: {}", e.toString());
        }
    }


    // Периодическая проверка новых кораблей


    public static void onServerTick(net.minecraft.server.MinecraftServer server) {
        if (++tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;
        checkForNewShips(server.overworld());
    }


    // Основная логика обнаружения новых кораблей

    private static java.lang.reflect.Method cachedGetAllShips = null;

    public static void checkForNewShips(ServerLevel level) {
        try {
            Object shipWorld = level.getClass().getMethod("getShipObjectWorld").invoke(level);
            if (shipWorld == null) return;

            if (cachedGetAllShips == null) {
                cachedGetAllShips = shipWorld.getClass().getMethod("getAllShips");
            }

            Iterable<?> ships = (Iterable<?>) cachedGetAllShips.invoke(shipWorld);
            Set<String> currentShipIds = new HashSet<>();

            for (Object ship : ships) {
                try {
                    String shipId = ship.getClass().getMethod("getId").invoke(ship).toString();
                    currentShipIds.add(shipId);
                    if (knownShipIds.contains(shipId)) continue;
                    knownShipIds.add(shipId);

                    // Пропускаем уже зарегистрированные корабли
                    if (UnregisteredShipsManager.contains(shipId)) continue;
                    // Пропускаем корабли которые уже зарегистрированы
                    if (RegisteredShipsManager.getRegisteredName(shipId) != null) continue;

                    String slug = getSlug(ship);

                    UnregisteredShipsManager.addShip(shipId, slug);
                    LOGGER.info("Новый корабль обнаружен: id={} slug={}", shipId, slug);

                } catch (Exception e) {
                    LOGGER.warn("Ошибка обработки корабля: {}", e.toString());
                }
            }

            for (String shipId : UnregisteredShipsManager.getShipIds()) {
                if (currentShipIds.contains(shipId)) continue;
                UnregisteredShipsManager.removeShip(shipId);
                knownShipIds.remove(shipId);
                LOGGER.info("Корабль исчез из мира, удалён из незарегистрированных: {}", shipId);
            }


            // Clean up registered ships deleted via VS
            java.util.List<String> registeredIds = new java.util.ArrayList<>(RegisteredShipsManager.getAllRegisteredShips().keySet());
            for (String registeredShipId : registeredIds) {
                if (currentShipIds.contains(registeredShipId)) continue;
                RegisteredShipsManager.ShipRegistration reg = RegisteredShipsManager.getRegistration(registeredShipId);
                RegisteredShipsManager.unregisterShip(registeredShipId);
                knownShipIds.remove(registeredShipId);
                if (reg != null && reg.ownerUuid != null) {
                    try {
                        UUID ownerId = UUID.fromString(reg.ownerUuid);
                        Claim claim = ClaimManager.getClaimByShipId(level, registeredShipId);
                        if (claim != null && claim.isActive()) {
                            VsClaimManager.releaseShipClaimSlot(level, ownerId);
                        }
                        if (claim != null) {
                            ClaimManager.removeClaim(level, claim.getCenter());
                        }
                    } catch (Exception ex) {
                        LOGGER.warn("cleanupRegistered: {}", ex.toString());
                    }
                }
                LOGGER.info("Registered ship deleted via VS, cleaned up: {}", registeredShipId);
            }
        } catch (Exception e) {
            LOGGER.warn("checkForNewShips: {}", e.toString());
        }
    }


    private static String getSlug(Object ship) {
        try {
            Object slug = ship.getClass().getMethod("getSlug").invoke(ship);
            return slug != null ? slug.toString() : "ship";
        } catch (Exception ignored) {
            return "ship";
        }
    }

}