package ru.javaops.masterjava.users;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.tweak.ConnectionFactory;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.events.XMLEvent;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Дмитрий on 31.03.2017.
 * try to import users from xml and store in DB
 */
public class XmlImporterTest {

    private List<User> users = new ArrayList<>();
    static Map<Integer, User> invalidUsers = new HashMap<>();

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
        UserDao DAO = DBIProvider.getDao(UserDao.class);
        DAO.clean();

        StaxStreamProcessor processor = new StaxStreamProcessor(Resources.getResource("payload.xml").openStream());
        int count = 0; //just for idea - wan't see "duplicated code" underlining
        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            count++;
            final String email = processor.getAttribute("email");
            final UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
            final String fullName = processor.getReader().getElementText();
            final User user = new User(fullName, email, flag);
            users.add(user);
        }
        users.forEach(DAO::insert);
    }
}
