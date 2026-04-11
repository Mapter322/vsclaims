package com.mapter.vsclaims.ship;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class UnregisteredShipsManager {

    private static final Logger LOGGER = LogManager.getLogger("vsclaims/UnregisteredShipsManager");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "unclaimed_ships.json";

    private static final Map<String, UnregisteredShip> ships = new ConcurrentHashMap<>();
    private static Path saveFile = null;


    // Структура записи

    public static class UnregisteredShip {
        public final String shipId;
        public final String slug;
        public final String createdAt;

        public UnregisteredShip(String shipId, String slug) {
            this.shipId = shipId;
            this.slug = slug;
            this.createdAt = Instant.now().toString();
        }

        public UnregisteredShip(String shipId, String slug, String createdAt) {
            this.shipId = shipId;
            this.slug = slug;
            this.createdAt = createdAt;
        }
    }


    // Forge события

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        Path dataDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .toAbsolutePath().resolve("vsclaims");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            LOGGER.error("Не удалось создать директорию vsclaims: {}", e.toString());
        }
        saveFile = dataDir.resolve(FILE_NAME);
        load();
        LOGGER.info("UnregisteredShipsManager загружен, кораблей: {}", ships.size());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        save();
    }


    // Публичное API

    public static void addShip(String shipId, String slug) {
        if (ships.containsKey(shipId)) return;
        ships.put(shipId, new UnregisteredShip(shipId, slug));
        save();
        LOGGER.info("Добавлен незарегистрированный корабль: id={} slug={}", shipId, slug);
    }

    public static void removeShip(String shipId) {
        if (ships.remove(shipId) != null) {
            save();
            LOGGER.info("Корабль {} удалён из незарегистрированных (зарегистрирован)", shipId);
        }
    }

    public static boolean contains(String shipId) {
        return ships.containsKey(shipId);
    }

    public static Collection<UnregisteredShip> getAll() {
        return Collections.unmodifiableCollection(ships.values());
    }

    public static Set<String> getShipIds() {
        return Collections.unmodifiableSet(new HashSet<>(ships.keySet()));
    }

    public static int getCount() {
        return ships.size();
    }


    // Сохранение / загрузка

    public static void save() {
        if (saveFile == null) return;
        try {
            JsonArray array = new JsonArray();
            for (UnregisteredShip s : ships.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("shipId",    s.shipId);
                obj.addProperty("slug",      s.slug);
                obj.addProperty("createdAt", s.createdAt);
                array.add(obj);
            }
            Files.writeString(saveFile, GSON.toJson(array));
        } catch (IOException e) {
            LOGGER.error("Ошибка сохранения {}: {}", FILE_NAME, e.toString());
        }
    }

    private static void load() {
        ships.clear();
        if (saveFile == null || !Files.exists(saveFile)) return;
        try {
            String content = Files.readString(saveFile);
            JsonArray array = JsonParser.parseString(content).getAsJsonArray();
            for (JsonElement el : array) {
                JsonObject obj = el.getAsJsonObject();
                String shipId    = obj.get("shipId").getAsString();
                String slug      = obj.has("slug")      ? obj.get("slug").getAsString()      : "";
                String createdAt = obj.has("createdAt") ? obj.get("createdAt").getAsString()  : "";
                ships.put(shipId, new UnregisteredShip(shipId, slug, createdAt));
            }
        } catch (Exception e) {
            LOGGER.error("Ошибка загрузки {}: {}", FILE_NAME, e.toString());
        }
    }
}