import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;


public class SentimentConsumer extends Thread {

    private static final String QUEUE_NAME = "Sentiment";

    private Channel channel;
    private BasicDataSource dataPool;

    public SentimentConsumer(com.rabbitmq.client.Connection connection, BasicDataSource dataSource) throws IOException {
        channel = connection.createChannel();
        dataPool = dataSource;
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String URI = new String(delivery.getBody(), "UTF-8");
            String[] uriElements = URI.split("/");
            Integer album = Integer.valueOf(uriElements[uriElements.length-1]);
            String sentiment = uriElements[uriElements.length-2];
            try (java.sql.Connection con = dataSource.getConnection()) {
                String query = sentiment.equalsIgnoreCase("like") ?
                        "UPDATE albums SET likes = likes + 1 WHERE album_id == ?":
                        "UPDATE albums SET dislikes = dislikes + 1 WHERE album_id == ?";
                try(PreparedStatement preparedStatement = con.prepareStatement(query)) {
                    preparedStatement.setInt(1,album);
                    preparedStatement.executeQuery();
                } catch (Exception e) {e.printStackTrace();}
            } catch (Exception e) {e.printStackTrace();}
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
