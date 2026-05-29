package app.tshepo;

import app.tshepo.config.AsyncSyncConfiguration;
import app.tshepo.config.EmbeddedSQL;
import app.tshepo.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        TshepoVaultApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        app.tshepo.config.JacksonHibernateConfiguration.class,
    }
)
@EmbeddedSQL
public @interface IntegrationTest {}
