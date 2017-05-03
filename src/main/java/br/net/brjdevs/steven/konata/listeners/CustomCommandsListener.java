package br.net.brjdevs.steven.konata.listeners;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.data.DataManager;
import br.net.brjdevs.steven.konata.core.data.guild.CustomCommand;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.events.EventListener;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomCommandsListener extends EventListener<GuildMessageReceivedEvent> {

    private static final Random r = new Random();

    private static Pattern RANDOM_PATTERN = Pattern.compile("(\\$random\\{.+?;+.+?})", Pattern.CASE_INSENSITIVE);

    public CustomCommandsListener() {
        super(GuildMessageReceivedEvent.class);
    }
    @Override
    public void onEvent(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isFake() || !event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_WRITE, Permission.MESSAGE_READ))
            return;
        GuildData data = GuildData.of(event.getGuild());
        String prefix, msg = event.getMessage().getRawContent();
        if (msg.startsWith(prefix = KonataBot.getInstance().getConfig().defaultPrefix) || data.getCustomPrefix() != null && msg.startsWith(prefix = data.getCustomPrefix())) {
            String alias = msg.substring(prefix.length()).split(" ")[0];
            CustomCommand cmd = DataManager.db().getCustomCommand(event.getGuild(), alias);
            if (cmd != null) {
                String answer = replace(cmd.getRandomAnswer(), event.getMember(), msg.substring(prefix.length() + alias.length()).trim(), event);
                if (!answer.isEmpty())
                    event.getChannel().sendTyping().queue(sent -> event.getChannel().sendMessage(answer).queue());
            }
        }
    }

    private String replace(String s, Member member, String args, GuildMessageReceivedEvent event) {
        s = s.replace("%user%", member.getUser().getName()).replace("%mention%", member.getUser().getAsMention()).replace("%guild%", member.getGuild().getName()).replace("%id%", member.getUser().getName()).replace("%input%", args);
        Matcher matcher = RANDOM_PATTERN.matcher(s);
        while (matcher.find()) {
            String group = matcher.group(0);
            String[] options = group.substring(group.indexOf("{") + 1, group.lastIndexOf("}")).split(";");
            int random = r.nextInt(options.length - 1);
            group = Pattern.quote(group);
            s = s.replaceFirst(group, options[random]);
        }
        return s;
    }
}
