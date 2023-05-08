package com.worldbuilder.mapgame;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineSetup {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String MATCHMAKING_COLLECTION = "matchmaking";

    private void createNewGameSession(String playerName) {
        MapGenerator mapGenerator = new MapGenerator();
        Tile[][] map = mapGenerator.generateRandomMap(1000, 1000, 0f, 0f);
        List<Map<String, Object>> serializedMap = serializeTileArray(map);


        Map<String, Object> gameSession = new HashMap<>();

        gameSession.put("map", serializedMap);
        gameSession.put("player1", playerName);
        gameSession.put("player2", null);
        gameSession.put("status", "waiting");

        db.collection(MATCHMAKING_COLLECTION)
                .add(gameSession)
                .addOnSuccessListener(documentReference -> {
                    listenForGameUpdates(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    // Find an available game session
    private void findAvailableGameSession(String playerName) {
        db.collection(MATCHMAKING_COLLECTION)
                .whereEqualTo("status", "waiting")
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        joinGameSession(document.getId(), playerName);
                    } else {
                        createNewGameSession(playerName);
                    }
                });
    }

    // Join an existing game session
    private void joinGameSession(String gameSessionId, String playerName) {
        DocumentReference gameSessionRef = db.collection(MATCHMAKING_COLLECTION).document(gameSessionId);
        gameSessionRef.update("player2", playerName, "status", "ready")
                .addOnSuccessListener(aVoid -> {

                    listenForGameUpdates(gameSessionId);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    // Listen for updates to the game session
    private void listenForGameUpdates(String gameSessionId) {
        DocumentReference gameSessionRef = db.collection(MATCHMAKING_COLLECTION).document(gameSessionId);
        gameSessionRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                // Handle error
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                // Update game state based on the snapshot data
            } else {
                // Handle the game session being deleted or not existing
            }
        });
    }

    // Serialize the 2D array of Tile objects into a List of Maps
    private List<Map<String, Object>> serializeTileArray(Tile[][] tileArray) {
        List<Map<String, Object>> serializedTiles = new ArrayList<>();

        for (Tile[] row : tileArray) {
            for (Tile tile : row) {
                serializedTiles.add(tile.toMap());
            }
        }

        return serializedTiles;
    }

    // Deserialize the List of Maps into a 2D array of Tile objects
    private Tile[][] deserializeTileArray(List<Map<String, Object>> serializedTiles, int numRows, int numCols) {
        Tile[][] tileArray = new Tile[numRows][numCols];
        int index = 0;

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                tileArray[row][col] = Tile.fromMap(serializedTiles.get(index));
                index++;
            }
        }

        return tileArray;
    }

    // Save the serialized map data to Firestore
    private void saveMapDataToFirestore(String gameSessionId, Tile[][] tileArray) {
        List<Map<String, Object>> serializedTiles = serializeTileArray(tileArray);
        DocumentReference gameSessionRef = db.collection("game_sessions").document(gameSessionId);

        gameSessionRef.update("mapData", serializedTiles)
                .addOnSuccessListener(aVoid -> {
                    // Map data saved successfully
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    // Load the map data from Firestore
    private void loadMapDataFromFirestore(String gameSessionId, int numRows, int numCols) {
        DocumentReference gameSessionRef = db.collection("game_sessions").document(gameSessionId);
        gameSessionRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> serializedTiles = (List<Map<String, Object>>) documentSnapshot.get("mapData");
                    Tile[][] tileArray = deserializeTileArray(serializedTiles, numRows, numCols);

                    // Update your game state with the loaded map data
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }
}
