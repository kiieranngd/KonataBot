package br.net.brjdevs.steven.konata.cmds.misc;

import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import net.dv8tion.jda.core.EmbedBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WeatherCommand {

    @RegisterCommand
    public static ICommand weather() {
        return new ICommand.Builder()
                .setAliases("weather")
                .setName("Weather Command")
                .setDescription("Queries weather from almost everywhere!")
                .setAction((event) -> {
                    JSONObject object;
                    try {
                        object =  search(event.getArguments());
                        if (object == null) {
                            throw new IOException("Wtf?");
                        }
                    } catch (IOException e) {
                        event.sendMessage(e.getMessage()).queue();
                        return;
                    } catch (JSONException e) {
                        event.sendMessage(Emojis.X + " I didn't find anything by `" + event.getArguments() + "`! Are you sure you spelled it correctly?").queue();
                        return;
                    } catch (RuntimeException e) {
                        event.sendMessage("Yahoo api took too long to respond! " + Emojis.CONFUSED).queue();
                        return;
                    }
                    JSONObject
                            wind = object.getJSONObject("wind"),
                            atmosphere = object.getJSONObject("atmosphere"),
                            astronomy = object.getJSONObject("astronomy");

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(object.getString("title"), object.getString("link"));
                    embedBuilder.addField("\uD83C\uDF2C Wind", "**Speed:** " + wind.getString("speed") + "mph  **Chill:** " + wind.getString("chill"), false);
                    embedBuilder.addField("\uD83C\uDF25 Atmosphere", "**Humidity:** " + atmosphere.getString("humidity") + "  **Pressure:** " + atmosphere.getString("pressure"), false);
                    embedBuilder.addField("\uD83C\uDFDD Astronomy", "**Sunrise:** " + astronomy.getString("sunrise") + "  **Sunset:** " + astronomy.getString("sunset"), false);
                    embedBuilder.setFooter("Last update: " + object.getString("lastBuildDate"), null);
                    embedBuilder.setColor(Color.decode("#388BDF"));
                    event.sendMessage(embedBuilder.build()).queue();

                })
                .build();
    }


    public static JSONObject search(String query) throws IOException {
        try {
            String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22" + URLEncoder.encode(query, "UTF-8") + "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
            FutureTask<String> task = new FutureTask<>(() -> {
                try {
                    HttpClient client = HttpClientBuilder.create().build();
                    HttpGet get = new HttpGet(url);
                    return EntityUtils.toString(client.execute(get).getEntity());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });
            task.run();
            Object obj;
            try {
                obj = task.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                throw new RuntimeException("Yahoo API didn't respond.");
            }
            if (obj == null) {
                throw new IOException("Error while querying.");
            }
            return new JSONObject((String) obj).getJSONObject("query").getJSONObject("results").getJSONObject("channel");
        } catch (UnsupportedEncodingException ignored) {
            return null;
        } catch (JSONException | NullPointerException e) {
            throw new JSONException("Not found.");
        }
    }
}
