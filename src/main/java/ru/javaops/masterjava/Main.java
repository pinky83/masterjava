package ru.javaops.masterjava;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.Project;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;

import java.util.*;

/**
 * User: gkislin
 * Date: 05.08.2015
 *
 * @link http://caloriesmng.herokuapp.com/
 * @link https://github.com/JavaOPs/topjava
 */
public class Main {
    private static final JaxbParser PARSER = new JaxbParser(ObjectFactory.class);
    static {
        PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    public static void getUsers(String projectName) throws Exception{
        Payload payload = PARSER.unmarshal(Resources.getResource("payload.xml").openStream());
        Set<User> users = new TreeSet<>(Comparator.comparing(User::getFullName));

        List<Project> projects = payload.getProjects().getProject();
        projects.forEach(p -> {
            if (p.getTitle().equals(projectName))
                p.getGroups().getGroup().forEach(g -> g.getUsers().getUser().forEach(users::add));
        });

        if (users.size()==0) {
            System.out.println("No such project or no users in...");
            return;
        }
        users.forEach(System.out::println);
    }

    public static void main(String[] args) {
        System.out.format("Hello MasterJava!");

        try {
            getUsers("topjava");
        }catch (Exception e) {e.printStackTrace();}
    }
}
