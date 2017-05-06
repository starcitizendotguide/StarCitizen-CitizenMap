import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import okhttp3.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BackerTracker {

    private final static MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=UTF-8");
    private final static Gson GSON = new Gson();

    private final static File file = new File("src/main/resources/usernames.json");
    private final static Set<String> usernames = new HashSet<>();

    public static void main(String[] args) {

        //--- Load File
        System.out.println("[I/O] Loading the file.");
        try {
            loadFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        int[] lobbies = new int[] {1, 7, 13839, 13833, 13830, 13834, 17582, 30192, 52280, 22066, 8, 23541};

        while (true) {

            int i = usernames.size();
            final int[] x = {0};

            System.out.println("----------------------------------------");
            for (int lobby : lobbies) {

                //--- Request
                System.out.println(String.format("[HTTP-%d] Sending the request.", lobby));
                RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, "{\"lobby_id\": " + lobby + "}");

                Request request = new Request.Builder()
                        .url("https://robertsspaceindustries.com/api/spectrum/lobby/presences")
                        .post(body)
                        .addHeader("Agent", "Friendly Bot - Data Mining :)")
                        .build();

                try {

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            System.out.println(String.format("[HTTP-%d] Failed to receive a valid response.", lobby));
                            x[0]++;
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            System.out.println(String.format("[HTTP-%d] Successfully received the request's response. Parsing response now.", lobby));
                            JsonObject data = GSON.fromJson(response.body().string(), JsonObject.class);
                            data.getAsJsonArray("data").iterator().forEachRemaining(e -> BackerTracker.usernames.add(e.getAsJsonObject().get("nickname").getAsString()));
                            x[0]++;
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


            try {
                System.out.println("[BT] Waiting for all responses.");
                while (x[0] < lobbies.length) { Thread.sleep(10); } //I know... don't judge me ;(
                System.out.println("[BT] Received all responses.");

                //--- Save File
                System.out.println(String.format("[BT] Successfully added %d new usernames to the list. Total amount of users: %d (%.2f%%).", (usernames.size() - i), usernames.size(), (usernames.size() / 1_801_682D * 100)));
                System.out.println("[I/O] Saving the file.");
                saveFile();
                System.out.println("[BT] Sleeping for 60 seconds now.");
                System.out.println("----------------------------------------");
                Thread.sleep(60 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void loadFile() throws Exception {
        JsonArray array = GSON.fromJson(new JsonReader(new FileReader(file.getAbsolutePath())), JsonArray.class);

        BackerTracker.usernames.clear();
        array.iterator().forEachRemaining(e -> BackerTracker.usernames.add(e.getAsString()));
    }


    private static void saveFile() throws IOException {
        String json = GSON.toJson(usernames);
        if(json.getBytes("UTF-8").length > file.length()) {
            FileUtils.writeStringToFile(file, json, "UTF-8");
            System.out.println("[I/O] Successfully saved the file.");
        }
    }
}