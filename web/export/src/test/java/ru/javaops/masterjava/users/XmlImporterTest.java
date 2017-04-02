package ru.javaops.masterjava.users;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.events.XMLEvent;
import java.sql.DriverManager;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Дмитрий on 31.03.2017.
 * try to import users from xml and store in DB
 */
public class XmlImporterTest {

    private List<User> users = new ArrayList<>();
    private static Map<Integer, String> invalidUsers = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        DBIProvider.init(() -> {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("PostgreSQL driver not found", e);
            }
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/masterjava", "user", "password");
        });

    }

    @Test
    public void xmlImporterTest() throws Exception {
        //TODO replace nullable on Optional
        final UserDao DAO = DBIProvider.getDao(UserDao.class);
        DAO.clean();

        //email validation
        final String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

        final Pattern pattern = Pattern.compile(regex);
        Matcher matcher;

        StaxStreamProcessor processor = new StaxStreamProcessor(Resources.getResource("payload.xml").openStream());
        int count = 0; //counter of invalid users
        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            count++;
            User user;
            String fullName = null, email = null;
            try {
                email = processor.getAttribute("email");
                matcher = pattern.matcher(email);
                if((!matcher.matches())|| (email.equals(""))) email = null;
                UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
                fullName = processor.getReader().getElementText();
                if (fullName.equals(""))fullName = null;
                user = new User(fullName, email, flag);
            }catch (Exception e) {invalidUsers.put(count, fullName + " " + email);
                                  continue;
            }
            users.add(user);
        }

        for (User u : users) {
            try {
                DAO.insert(u);
            } catch (Exception e) {
                invalidUsers.put(count, u.getFullName() + " " + u.getEmail());
            }
        }

        //users.forEach(System.out::println);

        System.out.format("Total : %d, Invalid : %d%n", count, invalidUsers.size());

        invalidUsers.values().forEach(System.out::println);
    }
}
