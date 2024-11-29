package ru.peef.spleef;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.entity.Player;
import ru.peef.spleef.game.GameManager;
import ru.peef.spleef.game.GamePlayer;

import java.util.Date;
import java.util.UUID;

public class Database {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "cristalix";
    private static final String COLLECTION_PLAYERS_NAME = "players";
    private static final String COLLECTION_GAMES_NAME = "games";

    private final MongoCollection<Document> playerCollection;
    private final MongoCollection<Document> gamesCollection;

    public Database() {
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

        playerCollection = database.getCollection(COLLECTION_PLAYERS_NAME);
        gamesCollection = database.getCollection(COLLECTION_GAMES_NAME);
    }

    public void recordGame() {
        Document gameDoc = new Document("players_count", GameManager.getPlayers().size())
                .append("start_timestamp", GameManager.getStartTimestamp())
                .append("end_timestamp", System.currentTimeMillis());

        gamesCollection.insertOne(gameDoc);
    }

    public void addPlayer(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!hasPlayer(gamePlayer)) {
            Document playerDoc = new Document("uuid", uuid.toString())
                    .append("wins", 0)
                    .append("recent_game", null);
            playerCollection.insertOne(playerDoc);
        }
    }

    public void addWins(GamePlayer gamePlayer, int incrementWins) {
        Player player = gamePlayer.getPlayer();
        UUID uuid = player.getUniqueId();

        Document query = new Document("uuid", uuid.toString());
        Document update = new Document("$inc", new Document("wins", incrementWins))
                .append("$set", new Document("recent_game", new Date()));
        playerCollection.updateOne(query, update);
    }

    public Document getPlayer(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        UUID uuid = player.getUniqueId();

        Document query = new Document("uuid", uuid.toString());

        return playerCollection.find(query).first();
    }

    public boolean hasPlayer(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        UUID uuid = player.getUniqueId();

        Document query = new Document("uuid", uuid.toString());

        return playerCollection.find(query).first() != null;
    }

    public void removePlayer(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        UUID uuid = player.getUniqueId();

        Document query = new Document("uuid", uuid.toString());
        playerCollection.deleteOne(query);
    }
}
