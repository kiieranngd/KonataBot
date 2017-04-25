package br.net.brjdevs.steven.konata.cmds.botAdmin;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.rmi.UnexpectedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class EvalCommand {

    private static File folder = new File("classes");
    private static File f; // Src
    private static File out;
    private static final String imports;

    static {
        folder.mkdirs();
        f = new File(folder + "/DontUse.java");
        out = new File(folder + "/DontUse.class");
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("br.net.brjdevs.steven.konata"))));
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class).stream().filter(clazz -> clazz != null && clazz.getCanonicalName() != null).collect(Collectors.toSet());
        classes.addAll(reflections.getSubTypesOf(Enum.class));
        imports = classes.stream().map(clazz -> "import " + clazz.getCanonicalName() + ";").collect(Collectors.joining("\n")).replace("import br.net.brjdevs.steven.konata.core.commands.ICommand.Builder;", "");
    }

    private static String compile() throws Exception {
        if (!f.exists())
            throw new UnexpectedException("Unable to compile source file.");
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("javac", "-cp", System.getProperty("java.class.path"), folder + "/" + f.getName());
        Process p = builder.start();

        Scanner sc = new Scanner(p.getInputStream());

        Scanner scErr = new Scanner(p.getErrorStream());

        StringWriter writer = new StringWriter();
        IOUtils.copy(p.getErrorStream(), writer, Charset.forName("UTF-8"));
        String x = writer.toString().replace(System.getProperty("user.dir"), "<ClassPath>").replace("\\classes", "");

        sc.close();
        scErr.close();

        p.waitFor();
        p.destroyForcibly();

        return x;
    }

    private static String getBodyWithLines(String code) {
        String body =
                "import java.util.*;\n" +
                        "import java.math.*;\n" +
                        "import java.net.*;\n" +
                        "import java.io.*;\n" +
                        "import java.util.concurrent.*;\n" +
                        "import java.util.*;\n" +
                        "import java.util.regex.*;\n" +
                        "import java.time.*;\n" +
                        "import java.lang.*;\n" +
                        "import java.util.stream.*;\n" +
                        "import net.dv8tion.jda.core.entities.*;\n" +
                        "import net.dv8tion.jda.core.*;\n" +
                        "import net.dv8tion.jda.core.managers.*;\n" +
                        "import net.dv8tion.jda.core.managers.fields.*;\n" +
                        imports +
                        "public class " + f.getName().replace(".java", "") + "\n{" +
                        "\n\tpublic Object run() throws Exception" +
                        "\n\t{\n\t\t";
        String[] lines = code.split("\n");
        body += String.join("\n\t\t", (CharSequence[]) lines);
        body += (body.endsWith(";") ? "" : ";") + (!body.contains("return ") && !body.contains("throw ") ? ";return null;" : "") + "\n\t}"
                + "\n\n\tpublic void print(Object o) { System.out.print(o.toString()); }\n" +
                "\tpublic void println(Object o) { print(o.toString() + \"\\n\"); }\n" +
                "\tpublic void printErr(Object o) { System.err.print(o.toString()); }\n" +
                "\tpublic void printErrln(Object o) { printErr(o.toString() + \"\\n\"); }\n" +
                "\tprivate CommandEvent event;\n\n" +
                "\tpublic " + f.getName().replace(".java", "") + "(CommandEvent event)\n\t{\n" +
                "\t\tthis.event = event;\n\t}\n}";
        return body;
    }

    @RegisterCommand
    public static ICommand java() {
        return new ICommand.Builder()
                .setAliases("eval")
                .setOwnerOnly(true)
                .setName("Eval Command")
                .setDescription("Evaluates java expressions!")
                .setUsageInstruction("java [java expression]")
                .setCategory(Category.BOT_ADMIN)
                .setAction((event) -> {
                    Object x = null;
                    long runtime = 0, compile = 0;
                    try {
                        OutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
                        stream.write(getBodyWithLines(event.getArguments()).getBytes());
                        stream.close();
                        try {
                            FutureTask<?> task = new FutureTask<>(EvalCommand::compile);
                            compile = System.currentTimeMillis();
                            task.run();
                            x = task.get(15, TimeUnit.SECONDS);
                            compile = System.currentTimeMillis() - compile;
                        } catch (TimeoutException e) {
                            event.sendMessage("Compiling timed out.").queue();
                            return;
                        } catch (Exception e) {
                            event.sendMessage(e.getMessage()).queue();
                            return;
                        }
                        out.deleteOnExit();
                        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {folder.toURI().toURL()}, EvalCommand.class.getClassLoader());
                        Class clazz = urlClassLoader.loadClass(out.getName().replace(".class", ""));
                        if (clazz == null) {
                            event.sendMessage("Something went wrong trying to load the compiled class.").queue();
                            return;
                        }
                        Object o = clazz.getConstructors()[0].newInstance(event);
                        try {
                            Object finalO = o;
                            FutureTask<?> task = new FutureTask<>(() -> finalO.getClass().getMethod("run").invoke(finalO));
                            runtime = System.currentTimeMillis();
                            task.run();
                            o = task.get(2, TimeUnit.SECONDS);
                            runtime = System.currentTimeMillis() - runtime;
                        } catch (TimeoutException e) {
                            event.sendMessage("Method timed out.").queue();
                            return;
                        } catch (Exception e) {
                            o = e.getClass().getSimpleName() + ": " + e.getMessage();
                        }
                        if (o == null || o.toString().isEmpty())
                            o = "Executed without error and no objects returned!";
                        event.sendMessage("Took `" + compile + "ms` to compile and `" + runtime + "ms` to execute!\n\n**Result:** " + o.toString()).queue();
                    } catch (Exception e) {
                        if (x == null) x = e;
                        event.sendMessage("Something went wrong trying to eval your query.\n" + x).queue();
                    }
                    if (f.exists() && !f.delete()) {
                        event.sendMessage("Could not delete DontUse.java").queue();
                    }
                    if (out.exists() && !out.delete()) {
                        event.sendMessage("Could not delete DontUse.class").queue();
                    }
                })
                .build();
    }
}
