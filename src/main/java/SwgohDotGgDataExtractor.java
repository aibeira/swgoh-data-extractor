import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by mwerner on 1/4/17.
 */
public class SwgohDotGgDataExtractor {
    private List<Character> characters;
    private String swgohUsername;

    public SwgohDotGgDataExtractor(String swgohUsername) {
        this.swgohUsername = swgohUsername;
        characters = new ArrayList<Character>();
    }

    public void process() {
        getCharacterList();
    }

    private void getCharacterList() {
        String url = "https://swgoh.gg/u/" + swgohUsername + "/collection/";

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        String body = "";

        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            body = EntityUtils.toString(entity);
        }
        catch (Exception e) {

        }
        String patternString = "a href=\"/u/" + swgohUsername + "/collection/([\\w\\-]+)/\"";
        Pattern p = Pattern.compile(patternString);
        Matcher m = p.matcher(body);

        while (m.find()) {
            Character character = new Character();
            character.url = m.group(1);
            characters.add(character);
//            System.out.println(m.group());
            getCharacter(character);
        }
    }

    private void getCharacter(Character character) {
        String url = "https://swgoh.gg/u/" + swgohUsername + "/collection/" + character.url + "/";

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        String body = "";

        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            body = EntityUtils.toString(entity);
        }
        catch (Exception e) {

        }

        character.name = getName(body);

        if (character.name != null) {
            character.power = getPower(body);
            character.gearTier = getGearTier(body);
            character.level = getLevel(body);
            character.rarity = getRarity(body);
            System.out.printf("%s\t%d\t%d\t%d\t%d\n", character.name, character.power, character.level, character.gearTier, character.rarity);
        } else {
            characters.remove(character);
        }
    }

    private String getName(String body) {
        // <title>aibeira's Stormtrooper Han on Star Wars Galaxy of Heroes · SWGOH.GG</title>
        Pattern pattern = Pattern.compile("<title>" + swgohUsername + "'s (.*) on Star Wars Galaxy of Heroes · SWGOH.GG</title>");
        Matcher matcher = pattern.matcher(body);

        while (matcher.find()) {
            return StringEscapeUtils.unescapeHtml4(matcher.group(1));
        }

        return null;
    }

    private int getPower(String body) {
        // <span class="pc-stat-value">9001</span><h5>Power</h5>
        Pattern pattern = Pattern.compile("<span class=\"pc-stat-value\">(\\d+)</span><h5>Power</h5>");
        Matcher matcher = pattern.matcher(body);

        while (matcher.find()) {
            return Integer.parseInt(StringEscapeUtils.unescapeHtml4(matcher.group(1)));
        }

        return 0;
    }

    private int getLevel(String body) {
        // <div class="char-portrait-full-level">85</div>
        Pattern pattern = Pattern.compile("<div class=\"char-portrait-full-level\">(\\d+)</div>");
        Matcher matcher = pattern.matcher(body);

        while (matcher.find()) {
            return Integer.parseInt(StringEscapeUtils.unescapeHtml4(matcher.group(1)));
        }

        return 0;
    }

    private int getGearTier(String body) {
        // <div class="char-portrait-full-gear-level">XI</div>
        Pattern pattern = Pattern.compile("<div class=\"char-portrait-full-gear-level\">(\\w+)</div>");
        Matcher matcher = pattern.matcher(body);

        while (matcher.find()) {
            if (matcher.group(1).equals("I")) {
                return 1;
            } else if (matcher.group(1).equals("II")) {
                return 2;
            } else if (matcher.group(1).equals("III")) {
                return 3;
            } else if (matcher.group(1).equals("IV")) {
                return 4;
            } else if (matcher.group(1).equals("V")) {
                return 5;
            } else if (matcher.group(1).equals("VI")) {
                return 6;
            } else if (matcher.group(1).equals("VII")) {
                return 7;
            } else if (matcher.group(1).equals("VIII")) {
                return 8;
            } else if (matcher.group(1).equals("IX")) {
                return 9;
            } else if (matcher.group(1).equals("X")) {
                return 10;
            } else if (matcher.group(1).equals("XI")) {
                return 11;
            } else if (matcher.group(1).equals("XII")) {
                return 12;
            } else if (matcher.group(1).equals("XIII")) {
                return 13;
            }
        }

        return 0;
    }

    private int getRarity(String body) {
        // <div class="star star1"></div><div class="star star2"></div><div class="star star3"></div><div class="star star4 star-inactive"></div><div class="star star5 star-inactive"></div><div class="star star6 star-inactive"></div><div class="star star7 star-inactive"></div><div class="char-portrait-full-level">36</div><div class="char-portrait-full-gear-level">IV</div>
        Pattern pattern = Pattern.compile("<div class=\"star star(\\d)(.)");
        Matcher matcher = pattern.matcher(body);

        int rarity = 0;

        while (matcher.find()) {
            int newRarity = Integer.parseInt(StringEscapeUtils.unescapeHtml4(matcher.group(1)));

            if (newRarity > rarity && !matcher.group(2).equals(" ")) {
                rarity = newRarity;
            }
        }

        return rarity;
    }

    private static class Character {
        public String url;
        public String name;
        public int power;
        public int gearTier;
        public int level;
        public int rarity;
    }
}
