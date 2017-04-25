package br.net.brjdevs.steven.konata.core.data;

import br.net.brjdevs.steven.konata.core.utils.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public String token, game, streamUrl, dbotsOrg, dbotsPw, defaultPrefix;
    public List<String> owners;
    public int corePoolSize;

    public Config(Path path) throws IOException {
        if (!path.toFile().exists()) {
            if (!path.toFile().createNewFile())
                throw new IOException("Could not create file");
            else {
                JSONObject object = new JSONObject();
                object.put("token", "");
                object.put("game", "");
                object.put("streamUrl", "");
                object.put("dbotsOrg", "");
                object.put("dbotsPw", "");
                object.put("owners", new ArrayList<>());
                object.put("corePoolSize", 5);
                object.put("defaultPrefix", "!");
                IOUtils.write(path, object.toString());
                return;
            }
        }
        JSONObject json = new JSONObject(IOUtils.read(path));
        this.token = json.getString("token");
        this.owners = new ArrayList<>();
        for (Object o : json.getJSONArray("owners")) {
            if (o instanceof String)
                this.owners.add((String) o);
        }
        this.game = json.getString("game");
        this.streamUrl = json.getString("streamUrl");
        this.corePoolSize = json.getInt("corePoolSize");
        this.dbotsOrg = json.getString("dbotsOrg");
        this.dbotsPw = json.getString("dbotsPw");
        this.defaultPrefix = json.getString("defaultPrefix");
    }
}
