import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
@WebServlet(name = "SentimentServlet", urlPatterns = "/review/*")
public class SentimentServlet extends HttpServlet {



    private static final String QUEUE_NAME = "Sentiment";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String URI = req.getRequestURI();
        if (URI == null || URI.isEmpty() || !validateURI(URI)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(JsonUtils.objectToJson(new ErrorMsg("Invalid URL")));
            return;
        }
        RabbitMQChannelPool pool = null;
        Channel channel = null;
        try {
            pool = (RabbitMQChannelPool) getServletContext().getAttribute("channelPool");
            channel = pool.borrowChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, URI.getBytes());
        } catch (Exception e) {
            res.getWriter().write(JsonUtils.objectToJson(new ErrorMsg(e.getMessage())));
            res.setStatus(400);
        } finally {
            if (pool != null && channel != null) {
                pool.returnChannel(channel);
            }
        }
        res.setStatus(HttpServletResponse.SC_OK);
    }

    boolean validateURI(String URI) {
        String regex = ".*review/(like|dislike)/\\d+$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(URI);
        return matcher.matches();
    }
}
