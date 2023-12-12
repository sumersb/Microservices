import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


@WebServlet(name = "SentimentServlet", urlPatterns = "/review/*")
public class SentimentServlet extends HttpServlet {

    private static final String QUEUE_NAME = "Sentiment";
    private static final String rabbitMQURL = "localhost";
    private static final int poolSize = 30;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private RabbitMQChannelPool channelPool;

    public SentimentServlet() throws ServletException, IOException, TimeoutException {
        super.init();
        connectionFactory = createRabbitMQConnectionFactory();
        connection = connectionFactory.newConnection();
        channelPool = new RabbitMQChannelPool(connection, poolSize,QUEUE_NAME);

    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //Extract URI from doPost request
        String URI = req.getRequestURI();

        //Validate URI to make sure correct format
        if (URI == null || URI.isEmpty() || !validateURI(URI)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(JsonUtils.objectToJson(new ErrorMsg("Invalid URL")));
            return;
        }

        Channel channel = null;
        try {
            //Borrow channel from pool
            channel = channelPool.borrowChannel();
            try {
                channel.basicPublish("", QUEUE_NAME, null, URI.getBytes());
            } catch (Exception e) {
                System.out.println("Channel unable to be published to");
            }

            res.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            //Checks for errors if error occurs
            System.out.println("Unable to get pool or borrow channel from pool");
            System.out.println(e.getMessage());
            res.getWriter().write(JsonUtils.objectToJson(new ErrorMsg(e.getMessage())));
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            //Returns channel to pool
            if (channel != null) {
                channelPool.releaseChannel(channel);
            }
        }
    }


    boolean validateURI(String URI) {
        String regex = ".*review/(like|dislike)/\\d+$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(URI);
        return matcher.matches();
    }

    private ConnectionFactory createRabbitMQConnectionFactory() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQURL);
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }
}
