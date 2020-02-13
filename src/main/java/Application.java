import com.opentok.OpenTok;
import com.opentok.Session;
import com.opentok.exception.OpenTokException;
import org.jose4j.json.internal.json_simple.JSONObject;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;

public class Application {

    private static OpenTok opentok;
    private static Session stableSession;
    private static String apiKey = System.getProperty("API_KEY");
    private static String apiSecret = System.getProperty("API_SECRET");

    public static void main(String[] args) throws OpenTokException {
        if (apiKey == null || apiSecret == null)
            throw new RuntimeException("API KEY AND API SECRET MUST NOT BE NULL. SET OS ENVS");

        opentok = new OpenTok(Integer.parseInt(apiKey), apiSecret);
        stableSession = opentok.createSession();

        get(new FreeMarkerTemplateView("/") {
            public ModelAndView handle(Request request, Response response) {
                String token = null;
                try {
                    token = stableSession.generateToken();
                } catch (OpenTokException e) {
                    e.printStackTrace();
                }

                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("apiKey", apiKey);
                attributes.put("sessionId", stableSession.getSessionId());
                attributes.put("token", token);

                return new ModelAndView(attributes, "index.ftl");
            }
        });

        get(new Route("/createSession") {
            public JSONObject handle(Request request, Response response) {
                Map<String, Object> attributes = new HashMap<String, Object>();
                try {
                    Session session = opentok.createSession();
                    String token = session.generateToken();
                    attributes.put("apiKey", apiKey);
                    attributes.put("sessionId", session.getSessionId());
                    attributes.put("token", token);
                } catch (OpenTokException e) {
                    e.printStackTrace();
                }

                return new JSONObject(attributes);
            }
        });
    }
}
