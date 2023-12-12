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
        System.out.println("Im getting hit");
        //Extract URI from doPost request
        String URI = req.getRequestURI();

        //Validate URI to make sure correct format
        if (URI == null || URI.isEmpty() || !validateURI(URI)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(JsonUtils.objectToJson(new ErrorMsg("Invalid URL")));
            return;
        }

        String[] uriSplit = URI.split("/");
        Integer albumID = Integer.valueOf(uriSplit[uriSplit.length-1]);

        MaxUpdater maxUpdater = (MaxUpdater) getServletContext().getAttribute("maxUpdater");

        if ((Integer)maxUpdater.getMax() < albumID) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(JsonUtils.objectToJson(new ErrorMsg("Album with ID does not exist")));
            return;
        }
        //Initialize pool and channel
        RabbitMQChannelPool pool = null;
        Channel channel = null;
        try {
            //Retrieve channelPool from ServletContext
            pool = (RabbitMQChannelPool) getServletContext().getAttribute("channelPool");

            //Borrow channel from pool
            channel = pool.borrowChannel();

            //Declare Queue if not already created and send URI to queue
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, URI.getBytes());

            res.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            //Checks for errors if error occurs
            System.out.println(e.getMessage());
            res.getWriter().write(JsonUtils.objectToJson(new ErrorMsg(e.getMessage())));
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            //Returns channel to pool
            if (pool != null && channel != null) {
                pool.releaseChannel(channel);
            }
        }
    }

    boolean validateURI(String URI) {
        String regex = ".*review/(like|dislike)/\\d+$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(URI);
        return matcher.matches();
    }
}
