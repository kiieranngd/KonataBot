package br.net.brjdevs.steven.konata.core.weather;

import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import org.json.JSONObject;

public class Weather {
    private final String title;
    private final String description;
    private final double temp; //kelvin
    private final double pressure; //hpa
    private final double humidity; //%
    private final double windSpeed; //m/s
    private final long sunset, sunrise; //millis

    public Weather(JSONObject object) {
        JSONObject main = object.getJSONObject("main");
        this.description = StringUtils.capitalize(object.getJSONArray("weather").getJSONObject(0).getString("description"));
        this.temp = main.getDouble("temp");
        this.pressure = main.getDouble("pressure");
        this.humidity = main.getDouble("humidity");
        this.windSpeed = object.getJSONObject("wind").getDouble("speed");
        JSONObject sys = object.getJSONObject("sys");
        this.title = "Weather - " + object.getString("name") + ", " + sys.getString("country");
        this.sunset = sys.getLong("sunset");
        this.sunrise = sys.getLong("sunrise");
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }

    public double getTemperatureFarenheit() {
        return  ((temp - 273d) * 9d/5d) + 32d;
    }

    public double getTemperatureCelsius() {
        return temp - 273.16;
    }

    public double getPressure() {
        return pressure;
    }
    public double getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
    public String getSunrise() {
        long m = (sunrise / 60) % 60;
        long h = (sunrise / (60 * 60)) % 24;
        return String.format("%02d:%02d", h, m);
    }
    public String getSunset() {
        long m = (sunset / 60) % 60;
        long h = (sunset / (60 * 60)) % 24;
        return String.format("%02d:%02d", h, m);

    }
}
