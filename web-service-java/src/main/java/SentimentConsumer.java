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

    public SentimentConsumer(Channel channel, BasicDataSource dataSource) throws IOException {
        this.channel = channel;
        dataPool = dataSource;
    }

    @Override
    public void run() {
        try {
            //Declares queue if not already created
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            //Extract URI and process elements
            String URI = new String(delivery.getBody(), "UTF-8");
            String[] uriElements = URI.split("/");
            Integer album = Integer.valueOf(uriElements[uriElements.length-1]);
            String sentiment = uriElements[uriElements.length-2];

            //Get database connection
            try (java.sql.Connection con = dataPool.getConnection()) {

                //Sets query depending on whether sentiment is like or dislike
                String query = sentiment.equalsIgnoreCase("like") ?
                        "UPDATE albums SET likes = likes + 1 WHERE album_id = ?":
                        "UPDATE albums SET dislikes = dislikes + 1 WHERE album_id = ?";

                //Set parameters for preparedStatement
                try(PreparedStatement preparedStatement = con.prepareStatement(query)) {
                    preparedStatement.setInt(1,album);

                    //Execute preparedStatement
                    preparedStatement.executeUpdate();

                } catch (Exception e) {e.printStackTrace();}
            } catch (Exception e) {e.printStackTrace();}
        };
        try {
            //Consume queue at end of life
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
