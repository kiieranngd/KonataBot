package br.net.brjdevs.steven.konata.cmds.misc;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.weather.Weather;
import br.net.brjdevs.steven.konata.core.weather.WeatherUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WeatherCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger("Weather Command");

    @RegisterCommand
    public static ICommand weather() {
        return new ICommand.Builder()
                .setAliases("weather")
                .setName("Weather Command")
                .setDescription("Queries weather from almost everywhere!")
                .setCategory(Category.MISCELLANEOUS)
                .setAction((event) -> {
                    try {
                        Weather weather = WeatherUtils.query(event.getArguments());
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle(weather.getTitle(), null);
                        embedBuilder.addField("\uD83C\uDF2C Wind", "**Speed:** " + weather.getWindSpeed() + "m/s", false);
                        embedBuilder.addField("\uD83C\uDF25 Atmosphere", "**Humidity:** " + weather.getHumidity() + "%  **Pressure:** " + weather.getPressure(), false);
                        embedBuilder.addField("\uD83C\uDFDD Astronomy", "**Sunrise:** " + weather.getSunrise() + "  **Sunset:** " + weather.getSunset(), false);
                        embedBuilder.addField("\uD83C\uDF21 Weather", "**Temperature:** " + String.format("%.2f", weather.getTemperatureFarenheit()) + "ºF / " + String.format("%.2f", weather.getTemperatureCelsius()) + "ºC  **Description:** " + weather.getDescription(), false);
                        embedBuilder.setFooter("Powered by OpenWeatherMap API", null);
                        embedBuilder.setColor(Color.decode("#388BDF"));
                        event.sendMessage(embedBuilder.build()).queue();
                    } catch (Exception e) {
                        event.sendMessage("Oh well... Something went wrong when querying data! " + Emojis.SWEAT_SMILE ).queue();
                        LOGGER.error("Something failed when fetching data", e);
                    }

                })
                .build();
    }
}
