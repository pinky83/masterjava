package loader;

import xml.schema.FlagType;
import xml.schema.User;
import xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Дмитрий on 21.03.2017.
 *Util class for xml processing (STaX)
 */
class XmlLoaderForm {
    Collection<User> getUsersFromXML (InputStream stream) throws Exception{
        Collection<User> result = new HashSet<>();
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(stream)) {
            XMLStreamReader reader = processor.getReader();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if ("User".equals(reader.getLocalName())) {
                        User user = new User();
                        user.setFlag(FlagType.fromValue(reader.getAttributeValue(0)));
                        user.setEmail(reader.getAttributeValue(2));
                        user.setValue(reader.getElementText());
                        result.add(user);
                    }
                }
            }
        }
        return result;
    }
}
