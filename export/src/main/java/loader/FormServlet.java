package loader;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import xml.schema.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Дмитрий on 21.03.2017.
 *
 */
@WebServlet("/users")
@MultipartConfig
public class FormServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;
    private Collection<User> userList = Collections.EMPTY_LIST;
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Collection<Part> parts = req.getParts();
        XmlLoaderForm form = new XmlLoaderForm();

        for(Part part : parts) {
            try {
               userList = form.getUsersFromXML(part.getInputStream());
            }catch (Exception e) {e.printStackTrace();}
        }
        res.sendRedirect("/masterjava/users");
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        Template template = new Template(request.getServletContext());
        TemplateEngine engine = template.getEngine();
        WebContext ctx = new WebContext(request, response, request.getServletContext(),
                request.getLocale());
        ctx.setVariable("currentDate", LocalDate.now());
        ctx.setVariable("userList",userList);
        engine.process("users", ctx, response.getWriter()); //users.html, like .jsp processing
    }
}
