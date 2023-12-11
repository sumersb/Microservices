import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.dbcp2.BasicDataSource;
import com.rabbitmq.client.ConnectionFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener{
    private static final String databaseURL = "albumdb.c3hm6sf3alr0.us-west-2.rds.amazonaws.com";
    private static final String rabbitMQURL = "35.91.193.128";
    private static final int consumerCount = 3;
    private static final int channelSize = 20;
    private BasicDataSource dataSource;
    private ConnectionFactory connectionFactory;
    private Connection connection;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            dataSource = setupDataSource();
            sce.getServletContext().setAttribute("dataSource", dataSource);
            connectionFactory = createRabbitMQConnectionFactory();
            System.out.println(connectionFactory);
            sce.getServletContext().setAttribute("rabbitMQSource", connectionFactory);
            System.out.println("get here 3");
            connection = connectionFactory.newConnection();
            System.out.println(connection);
            System.out.println("get here 4");
            sce.getServletContext().setAttribute("connection", connection);
            System.out.println("get here 5");
            RabbitMQChannelPool channelPool = new RabbitMQChannelPool(connection, channelSize);
            System.out.println("get here 6");
            System.out.println(channelPool);
            System.out.println("get here 7");
            sce.getServletContext().setAttribute("channelPool", channelPool);
            System.out.println("get here 8");
            Thread[] consumers = new Thread[consumerCount];
            for (int i = 0; i < consumerCount; i++) {
                Channel channel = connection.createChannel();
                consumers[i] = new SentimentConsumer(channel,dataSource);
                consumers[i].start();
            }

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
        dataSource.setUrl("jdbc:mysql://"+databaseURL+":3306/album_info");
        dataSource.setUsername("root");
        dataSource.setPassword("password");

        // Optionally, you can configure additional properties, such as the initial size and max total connections
        dataSource.setInitialSize(5); // Set the initial number of connections
        dataSource.setMaxTotal(18);   // Set the maximum number of connections
        return dataSource;
    }

    private ConnectionFactory createRabbitMQConnectionFactory() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        System.out.println("1");
        factory.setHost(rabbitMQURL);
        System.out.println("2");
        factory.setPort(5672);
        System.out.println("3");
        factory.setUsername("guest");
        System.out.println("4");
        factory.setPassword("guest");
        System.out.println(factory);
        return factory;
    }

}
