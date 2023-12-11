import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.rabbitmq.client.Channel;


@WebServlet(name = "SentimentServlet", urlPatterns = "/review/*")
public class SentimentServlet extends HttpServlet {

    private static final String QUEUE_NAME = "Sentiment";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        System.out.println("here");
        String URI = req.getRequestURI();
        if (URI == null || URI.isEmpty() || !validateURI(URI)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(JsonUtils.objectToJson(new ErrorMsg("Invalid URL")));
            return;
        }
        System.out.println("hola");
        RabbitMQChannelPool pool = null;
        Channel channel = null;
        try {
            System.out.println("Satsrikal");
            pool = (RabbitMQChannelPool) getServletContext().getAttribute("channelPool");
            System.out.println(pool);
            channel = pool.borrowChannel();
            System.out.println(channel.isOpen());
            System.out.println("dmsapomdpsaompo");
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println("lemon");
            channel.basicPublish("", QUEUE_NAME, null, URI.getBytes());
            System.out.println("we made it");
        } catch (Exception e) {
            System.out.println("nadionsaoindosia");
            System.out.println(e.getMessage());
            res.getWriter().write(JsonUtils.objectToJson(new ErrorMsg(e.getMessage())));
            res.setStatus(400);
        } finally {
            if (pool != null && channel != null) {
                System.out.println("nsaodnaondiowqndomomqpwqdqwdwqdqw");
                pool.releaseChannel(channel);
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
