package loader;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;

/**
 * Created by Дмитрий on 21.03.2017.
 * Использует текущий контекст сервлета для создания движка шаблона
 */
class Template {
    private TemplateEngine engine;

    TemplateEngine getEngine() {
        return engine;
    }

    Template(ServletContext context) {
        ServletContextTemplateResolver templateResolver =
                new ServletContextTemplateResolver(context);
        templateResolver.setTemplateMode("XHTML");
        templateResolver.setPrefix("/WEB-INF/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);
        engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
    }
}
