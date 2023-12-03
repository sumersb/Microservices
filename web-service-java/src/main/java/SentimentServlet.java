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
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.getWriter().write("hello");
//    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String URI = req.getRequestURI();
        if (URI == null || URI.isEmpty() || !validateURI(URI)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(JsonUtils.objectToJson(new ErrorMsg("Invalid URL")));
            return;
        }

    }

    boolean validateURI(String URI) {
        String regex = ".*review/(like|dislike)/\\d+$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(URI);
        return matcher.matches();
    }
}
