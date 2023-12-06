import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.rabbitmq.client.Connection;
import org.apache.commons.dbcp2.BasicDataSource;
import com.rabbitmq.client.ConnectionFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@WebListener
public class AppInitializer implements ServletContextListener{
    private BasicDataSource dataSource;
    private ConnectionFactory connectionFactory;
    private Connection connection;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            dataSource = setupDataSource();
            sce.getServletContext().setAttribute("dataSource", dataSource);
            connectionFactory = createRabbitMQConnectionFactory();
            sce.getServletContext().setAttribute("rabbitMQSource", connectionFactory);
            connection = connectionFactory.newConnection();
            sce.getServletContext().setAttribute("connection", connection);
            RabbitMQChannelPool channelPool = new RabbitMQChannelPool(connection);
            sce.getServletContext().setAttribute("channelPool", channelPool);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup resources when the servlet context is destroyed
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (connectionFactory != null) {
            try {
                connectionFactory.clone();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static BasicDataSource setupDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://albumdb.c3hm6sf3alr0.us-west-2.rds.amazonaws.com:3306/album_info");
        dataSource.setUsername("root");
        dataSource.setPassword("password");

        // Optionally, you can configure additional properties, such as the initial size and max total connections
        dataSource.setInitialSize(5); // Set the initial number of connections
        dataSource.setMaxTotal(18);   // Set the maximum number of connections
        return dataSource;
    }

    private ConnectionFactory createRabbitMQConnectionFactory() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("54.245.0.230");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setRequestedChannelMax(10);
        // Other configuration options...
        return factory;
    }

}
