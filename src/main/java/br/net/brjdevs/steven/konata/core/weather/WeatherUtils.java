package br.net.brjdevs.steven.konata.core.weather;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.utils.IOUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

public class WeatherUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger("Weather Cache");

    private static final Cache<String, String> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(25)
            .build();

    public static Weather query(final String name) {
        return new Weather(new JSONObject(cache.asMap().computeIfAbsent(name.toLowerCase(), (n) -> {
            try {
                final String URL = "http://api.openweathermap.org/data/2.5/weather?q=" + encodeUrlNoExceptions(name)
                        + "&APPID=" + KonataBot.getInstance().getConfig().openWeatherApiKey;
                HttpClient client = HttpClientBuilder.create().build();
                HttpGet get = new HttpGet(URL);
                HttpResponse response = client.execute(get);
                return EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                return null;
            }
        })));
    }

    private static String encodeUrlNoExceptions(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }
}
