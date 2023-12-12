//import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import org.apache.commons.dbcp2.BasicDataSource;
//import com.rabbitmq.client.ConnectionFactory;
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;
//import java.io.IOException;
//import java.util.concurrent.TimeoutException;
//import javax.servlet.annotation.WebListener;
//
//@WebListener
//public class AppInitializer implements ServletContextListener{
//    private static final String databaseURL = "albumdb.c3hm6sf3alr0.us-west-2.rds.amazonaws.com";
//    private static final String rabbitMQURL = "52.43.224.97";
//    private static final String QUEUE_NAME = "Sentiment";
////    private static final int consumerCount = 150;
//    private static final int channelSize = 8;
//    private BasicDataSource dataSource;
//    private ConnectionFactory connectionFactory;
//    private Connection connection;
//    private MaxUpdater maxUpdater;
//
//    @Override
//    public void contextInitialized(ServletContextEvent sce) {
//        try {
//            maxUpdater = new MaxUpdater<Integer>();
//            sce.getServletContext().setAttribute("maxUpdater", maxUpdater);
//            dataSource = setupDataSource();
//            sce.getServletContext().setAttribute("dataSource", dataSource);
//            connectionFactory = createRabbitMQConnectionFactory();
//            sce.getServletContext().setAttribute("rabbitMQSource", connectionFactory);
//            connection = connectionFactory.newConnection();
//            RabbitMQChannelPool channelPool = new RabbitMQChannelPool(connection, channelSize, QUEUE_NAME);
//            sce.getServletContext().setAttribute("channelPool", channelPool);
//            //Thread[] consumers = new Thread[consumerCount];
////            for (int i = 0; i < consumerCount; i++) {
////                Channel channel = connection.createChannel();
////                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
////                consumers[i] = new SentimentConsumer(channel,dataSource);
////                consumers[i].start();
////            }
//            System.out.println("Successfully started");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void contextDestroyed(ServletContextEvent sce) {
//        // Cleanup resources when the servlet context is destroyed
//        if (dataSource != null) {
//            try {
//                dataSource.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (connectionFactory != null) {
//            try {
//                connection.close();
//                connectionFactory.clone();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private static BasicDataSource setupDataSource() {
//        BasicDataSource dataSource = new BasicDataSource();
//        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        dataSource.setUrl("jdbc:mysql://"+databaseURL+":3306/album_info");
//        dataSource.setUsername("root");
//        dataSource.setPassword("password");
//
//        // Optionally, you can configure additional properties, such as the initial size and max total connections
//        dataSource.setInitialSize(5); // Set the initial number of connections
//        dataSource.setMaxTotal(18);   // Set the maximum number of connections
//        return dataSource;
//    }
//
//    private ConnectionFactory createRabbitMQConnectionFactory() throws IOException, TimeoutException {
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost(rabbitMQURL);
//        factory.setPort(5672);
//        factory.setUsername("guest");
//        factory.setPassword("guest");
//        return factory;
//    }
//
//}
