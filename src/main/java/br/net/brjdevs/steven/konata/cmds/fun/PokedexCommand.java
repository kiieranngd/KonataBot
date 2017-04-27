package br.net.brjdevs.steven.konata.cmds.fun;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PokedexCommand {

    @RegisterCommand
    public static ICommand pokedex() {
        return new ICommand.Builder()
                .setAliases("pokedex")
                .setName("Pokedex Command")
                .setDescription("Gives you information about pokemon and abilities!")
                .setUsageInstruction("pokedex <pokemon_name> // gives you information on a pokemon\n" +
                        "pokedex ability <ability name> // gives you information on an ability.")
                .setCategory(Category.FUN)
                .setAction((event) -> {
                    String[] args = event.getArguments().split(" ", 2);
                    if (args.length < 2 && args[0].isEmpty()) {
                        event.sendMessage("What do you want to know? You have to provide a search term!").queue();
                        return;
                    }
                    String name, info;
                    switch (args[0]) {
                        case "ability":
                        case "a":
                            info = "ability";
                            name = args[1];
                            break;
                        default:
                            info = "pokemon";
                            if (args.length < 2)
                                name = args[0];
                            else
                                name = args[1];
                            break;
                    }
                    HttpClient client = HttpClientBuilder.create().build();
                    HttpGet get = new HttpGet("http://pokeapi.co/api/v2/" + info + "/" + name.toLowerCase().replaceAll(" ", "-"));
                    JSONObject object;
                    try {
                        object = new JSONObject(EntityUtils.toString(client.execute(get).getEntity()));
                    } catch (Exception e) {
                        event.sendMessage("Um... I didn't find a" + (info.equals("ability") ? "n" : "") + " " + info + " matching that criteria or pokeapi is offline.").queue();
                        return;
                    }

                    EmbedBuilder embedBuilder = new EmbedBuilder();

                    try {
                        switch (info) {
                            case "ability":
                                String generation = object.getJSONObject("generation").getString("name");
                                String[] splittedGeneration = generation.split("-");
                                generation = StringUtils.capitalize(splittedGeneration[0]) + " " + splittedGeneration[1].toUpperCase();
                                String effect = object.getJSONArray("effect_entries").getJSONObject(0).getString("effect");
                                List<String> pokemon = new ArrayList<>();
                                object.getJSONArray("pokemon").forEach((robj) -> {
                                    JSONObject obj = (JSONObject) robj;
                                    pokemon.add((obj.getBoolean("is_hidden") ? "*" : "") + obj.getJSONObject("pokemon").getString("name"));
                                });
                                embedBuilder.setTitle(StringUtils.capitalize(info) + " " + (name = StringUtils.capitalize(name)), get.getURI().toString());
                                embedBuilder.setDescription("First appeared in `" + generation + "`\n\n**Description:** " + effect + "\n\n**Pokemon with " + name + "**: " + pokemon.stream().map(s -> "`" + s + "`").collect(Collectors.joining(", ")) + "\n\n__\\* *Hidden ability*__");
                                embedBuilder.setFooter("Requested by " + StringUtils.toString(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl());
                                break;
                            case "pokemon":
                                int id = object.getInt("id");
                                String sprite = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png";
                                List<String> s = new ArrayList<>();
                                object.getJSONArray("abilities").forEach(ability -> {
                                    JSONObject o = (JSONObject) ability;
                                    s.add("`" + (o.getBoolean("is_hidden") ? "*" : "") + o.getJSONObject("ability").getString("name") + "`");
                                });
                                String abilities = String.join(", ", s) + "\n__\\**Hidden ability*__\n\n";
                                s.clear();
                                //HP/atk/def/sp atk/sp def/spd
                                List<Object> rawStats = object.getJSONArray("stats").toList();
                                Collections.reverse(rawStats);
                                String stats = rawStats.stream().map(obj -> String.valueOf(((Map) obj).get("base_stat"))).collect(Collectors.joining("/"));
                                embedBuilder.setThumbnail(sprite);
                                embedBuilder.setTitle(StringUtils.capitalize(info) + " " + StringUtils.capitalize(name), get.getURI().toString());
                                embedBuilder.appendDescription("Pokedex ID: " + id + "\n\n\n**Base stats:** " + stats + "\n\n**Abilities:** " + abilities);
                                embedBuilder.appendDescription("**Types:** " + object.getJSONArray("types").toList().stream().map(obj -> StringUtils.capitalize((String) ((Map) ((Map) obj).get("type")).get("name"))).collect(Collectors.joining(", ")) + "\n\n");
                                List<String> list = new ArrayList<>();
                                object.getJSONArray("moves").forEach(move -> {
                                    list.add("`" + StringUtils.capitalize(((JSONObject) move).getJSONObject("move").getString("name")).replace("-", " ") + "`");
                                });
                                embedBuilder.appendDescription("**Moves:** " + String.join(", ", list));
                                break;
                        }
                    } catch (Exception e) {
                        event.sendMessage("Um... I didn't find a" + (info.equals("ability") ? "n" : "") + " " + info + " matching that criteria or pokeapi is offline.").queue();
                    }

                    event.sendMessage(embedBuilder.build()).queue();
                })
                .build();
    }
}
