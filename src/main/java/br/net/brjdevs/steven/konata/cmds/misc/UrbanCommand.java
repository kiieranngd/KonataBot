package br.net.brjdevs.steven.konata.cmds.misc;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;

public class UrbanCommand {

    @RegisterCommand
    public static ICommand urban() {
        return new ICommand.Builder()
                .setAliases("urban", "ud")
                .setName("Urban Dictionary Command")
                .setDescription("Grabs definitions from www.urbandictionary.com.")
                .setCategory(Category.MISCELLANEOUS)
                .setAction((event) -> {
                    if (event.getArguments().isEmpty()) {
                        event.sendMessage("You didn't tell me a definition to search! Use `konata urban [term]").queue();
                        return;
                    }
                    try {
                        String query = URLEncoder.encode(event.getArguments(), "UTF-8");
                        HttpClient client = HttpClientBuilder.create().build();
                        HttpGet get = new HttpGet("http://api.urbandictionary.com/v0/define?term=" + query);
                        HttpResponse response = client.execute(get);
                        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
                        if(json.getString("result_type").equals("no_results")){
                            event.sendMessage("There aren't any definitions for `" + event.getArguments() + "` yet.").queue();
                            return;
                        }
                        JSONObject result = json.getJSONArray("list").getJSONObject(0);
                        String definition = result.getString("definition");
                        int thumbsup = result.getInt("thumbs_up");
                        String author = result.getString("author");
                        String example = result.getString("example");
                        int thumbsdown = result.getInt("thumbs_down");
                        String word = result.getString("word");
                        EmbedBuilder builder = new EmbedBuilder()
                                .setFooter("Powered by Urban Dictionary", "https://cdn.discordapp.com/attachments/225694598465454082/248910958280441868/photo.png")
                                .setTitle(word + " definition by " + author, "https://www.urbandictionary.com/define.php?term=" + query)
                                .addField("\uD83D\uDCD6 Definition: ", "**" + (definition == null || definition.isEmpty() ? "No definition provided." : definition.length() > MessageEmbed.VALUE_MAX_LENGTH ? "Definition is too big, click [here](https://www.urbandictionary.com/define.php?term=" + query + ") to see it." : definition) + "**", false)
                                .addField("\u2139 Example: ", example == null || example.isEmpty() ? "No example provided." : example.length() > MessageEmbed.VALUE_MAX_LENGTH ? "Example is too big, click [here](https://www.urbandictionary.com/define.php?term=" + query + ") to see it." : example, false)
                                .addField("\uD83D\uDC4D", String.valueOf(thumbsup), true)
                                .addField("\uD83D\uDC4E", String.valueOf(thumbsdown), true)
                                .setThumbnail("https://cdn1.iconfinder.com/data/icons/school-icons-2/512/open_book_pen_marker-256.png")
                                .setColor(Color.decode("#388BDF"));
                        event.sendMessage(builder.build()).queue();
                    } catch (MalformedURLException ignored) {
                    } catch (Exception e) {
                        event.sendMessage(Emojis.SWEAT_SMILE + " Something went wrong when processing your command!").queue();
                        LoggerFactory.getLogger(UrbanCommand.class).error("Shit happened in urban command, args: " + event.getArguments());
                    }
                })
                .build();
    }
}
