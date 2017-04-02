package ru.javaops.masterjava.export;

import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.sql.DriverManager;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * gkislin
 * 14.10.2016
 */
public class UserExport {

    static {
        DBIProvider.init(() -> {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("PostgreSQL driver not found", e);
            }
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/masterjava", "user", "password");
        });

    }

    private final UserDao DAO = DBIProvider.getDao(UserDao.class);
    //email validation
    private final String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    private List<User> users = new ArrayList<>();
    private static Map<Integer, String> invalidUsers = new HashMap<>();
    private final Pattern pattern = Pattern.compile(regex);

    public List<User> process(final InputStream is) throws XMLStreamException {

        final StaxStreamProcessor processor = new StaxStreamProcessor(is);

        int count = 0; //counter of invalid users
        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            count++;
            User user;
            String fullName = null;
            Optional<String> email = Optional.ofNullable(processor.getAttribute("email"));
            try {
                Matcher matcher = pattern.matcher(email.orElseThrow(IllegalArgumentException::new));
                if(!matcher.matches()){
                    fullName = processor.getReader().getElementText();
                    throw new Exception();
                }
                UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
                fullName = processor.getReader().getElementText();
                if (fullName.equals("")) throw new Exception();
                user = new User(fullName, email.orElse("no mail"), flag);
            }catch (Exception e) {
                if(!email.isPresent()){
                    try{
                        fullName = processor.getReader().getElementText();
                    }catch (Exception ex) {invalidUsers.put(count, "user " + count + " - empty name and email");
                        continue;}
                }
                invalidUsers.put(count,"user " + count + " " + fullName + " " + email.orElse("no email"));
                continue;
            }
            users.add(user);
        }

        DAO.clean();
        for (User u : users) {
            try {
                DAO.insert(u);
            } catch (Exception e) {
                invalidUsers.put(count, u.getFullName() + " " + u.getEmail());
            }
        }

        System.out.format("Total : %d, Invalid : %d%n", count, invalidUsers.size());
        invalidUsers.values().forEach(System.out::println);

        return users;
    }
}
