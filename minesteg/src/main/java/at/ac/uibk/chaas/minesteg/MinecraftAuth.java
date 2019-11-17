package at.ac.uibk.chaas.minesteg;

import com.google.gson.Gson;
import net.minecraft.util.Session;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * A class that offers methods for authenticating the Minecraft instance against
 * the Mojang servers.
 *
 * @author christoph.haas
 * @version 1.0.0
 * @since 1.8
 */
public class MinecraftAuth {
    private static final String PROPERTIES_FILE = "auth.properties";
    private static final String AUTH_ENDPOINT = "https://authserver.mojang.com/authenticate";

    private final String username;
    private final String password;

    /**
     * Constructor.
     */
    private MinecraftAuth() {
        Properties settings = PropertiesHelper.loadProperties(PROPERTIES_FILE);
        this.username = settings.getProperty("auth.username", "mcuser");
        this.password = settings.getProperty("auth.password", "");
    }

    /**
     * Get a modified session object including valid authentication tokens.
     *
     * @param originalSession the original session object.
     * @return the modified session object.
     */
    public static Session getAuthSession(Session originalSession) {
        MinecraftAuth auth = new MinecraftAuth();

        String[] authResult = auth.authenticate(auth.username, auth.password);
        if (authResult != null) {
            return new Session(authResult[0], authResult[1], authResult[2], authResult[3]);
        } else {
            return originalSession;
        }
    }

    /**
     * Authenticate against the Mojang API.
     *
     * @param username the username of the mojang account.
     * @param password the password of the mojang account.
     * @return an array containing authentication tokens.
     */
    private String[] authenticate(String username, String password) {

        String jsonRequest = "{" +
                "    \"agent\": {" +
                "        \"name\": \"Minecraft\"," +
                "        \"version\": 1" +
                "    }," +
                "    \"username\": \"" + username + "\"," +
                "    \"password\": \"" + password + "\"," +
                //"    \"clientToken\": \"client identifier\"," +
                "    \"requestUser\": true " +
                "}";

        try {
            URL url = new URL(AUTH_ENDPOINT);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            OutputStream os = connection.getOutputStream();
            os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            os.close();

            // read the response
            InputStream in = new BufferedInputStream(connection.getInputStream());
            String result = org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);
            System.out.println("Raw auth response: " + result);
            Map jsonResponse = new Gson().fromJson(result, Map.class);

            in.close();
            connection.disconnect();

            String accessToken = (String) jsonResponse.get("accessToken");
            String clientToken = (String) jsonResponse.get("clientToken");
            Map user = (Map) jsonResponse.get("user");
            Map selectedProfile = (Map) jsonResponse.get("selectedProfile");
            String userId = (String) user.get("id");
            String userName = (String) selectedProfile.get("name");
            String profileId = (String) selectedProfile.get("id");

            System.out.println("Auth response1: accessToken=" + accessToken + "; clientToken=" + clientToken);
            System.out.println("Auth response2: userId=" + userId + "; userName=" + userName + "; profileId=" + profileId);

            return new String[]{userName, profileId, accessToken, "legacy"};
        } catch (IOException e) {
            System.err.println("Authentication to mojang failed! " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}