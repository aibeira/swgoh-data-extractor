import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by mwerner on 1/19/17.
 */
public class HttpUtility {
    public static String performGet(String url) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        String body = "";

        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            body = EntityUtils.toString(entity);
        }
        catch (Exception e) {
            System.out.println("Error on perform get - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return body;
    }
}
