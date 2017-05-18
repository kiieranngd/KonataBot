package br.net.brjdevs.steven.konata;

import br.net.brjdevs.steven.konata.core.data.guild.Announces;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.utils.Mapifier;
import com.rethinkdb.net.Cursor;
import com.rethinkdb.net.Util;
import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.rethinkdb.RethinkDB.r;
import static br.net.brjdevs.steven.konata.core.data.DataManager.conn;

@SpringBootApplication
@RestController
public class WebServer {

    public static boolean ENABLED = false;

    @RequestMapping(value = "/api/guild", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> guild(@RequestParam("guildId") String guildId) {
        return new ResponseEntity<>(r.table(GuildData.DB_TABLE).get(guildId).toJson().run(conn()).toString(), HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/api/announces", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> announces(@RequestParam("guildId") String guildId) {
        return new ResponseEntity<>(r.table(Announces.DB_TABLE).get(guildId).toJson().run(conn()).toString(), HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/api/guilds", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> guilds(@RequestParam("userId") String userId) {
        String ids = KonataBot.getInstance().getGuilds()
                .stream().filter(guild -> guild.getMemberById(userId) != null)
                .map(Guild::getId).collect(Collectors.joining("|"));
        Cursor cursor = r.table(GuildData.DB_TABLE)
                .filter(data -> data.getField("id").match(ids))
                .run(conn());
        return new ResponseEntity<>(cursor.toList(), HttpStatus.ACCEPTED);
    }
}
