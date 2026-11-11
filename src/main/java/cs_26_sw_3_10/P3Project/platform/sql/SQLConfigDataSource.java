package cs_26_sw_3_10.P3Project.platform.sql;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Annotation
@Configuration

// Class
public class SQLConfigDataSource {

    @Value("${spring.datasource.url}")
    private void SetStaticUrl(String url) {
        SQLConfigDataSource.url = url;
    }

    static private String url;

    @Value("${spring.datasource.username}")
    private void SetStaticUsername(String username) {
        SQLConfigDataSource.username = username;
    }

    static private String username;

    @Value("${spring.datasource.password}")
    private void SetStaticPassword(String password) {
        SQLConfigDataSource.password = password;
    }

    static private String password;

    @Bean
    public static DataSource source() {
        DataSourceBuilder<?> dSB = DataSourceBuilder.create();
        // commented out for now.

        // Data base url
        dSB.url(url);

        // username credential
        dSB.username(username);

        // password credential
        dSB.password(password);

        return dSB.build();
    }
}