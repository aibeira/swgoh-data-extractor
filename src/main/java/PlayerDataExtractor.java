import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Created by mwerner on 1/4/17.
 */
public class PlayerDataExtractor {
    private List<Character> characters;
    private String swgohUsername;
    private String playerSwgohGgBody;
    private int level;
    private String playerName;
    private boolean loggingOn;

    public PlayerDataExtractor(String swgohGgUsername, boolean loggingOn) {
        this.swgohUsername = swgohGgUsername;
        characters = new ArrayList<Character>();
        this.loggingOn = loggingOn;

        extractPlayerProfile();
        extractPlayerName();
        extractPlayerLevel();
        extractCharacters();
    }

    public void getPowers() {
        if (loggingOn) System.out.print("Starting power extract...");
        int maxNameLength = -1;
        for(Character character: characters) {
            if (character.name.length() > maxNameLength) maxNameLength = character.name.length();
        }
        for (int i = 0; i < characters.size(); i++) {
            Character character = characters.get(i);
            if (loggingOn) System.out.print(String.format("\rGetting power for ( %3d / %3d ) %-" + maxNameLength + "s", i, characters.size(), character.name));
            getCharacterPower(character);
        }
        if (loggingOn) System.out.println(String.format("\rRetrieved power for %d characters.", characters.size()));
    }

    public String getName() { return playerName; }
    public int getLevel() { return level; }
    public List<Character> getCharacters() { return characters; }

    private void extractPlayerProfile() {
        String url = "https://swgoh.gg/u/" + swgohUsername + "/collection/";

        playerSwgohGgBody = HttpUtility.performGet(url).replace("\n", "");
    }

    private void extractPlayerName() {
        String patternString = "<title>([\\w ]+)'s.*</title>";
        Pattern p = Pattern.compile(patternString);
        Matcher m = p.matcher(playerSwgohGgBody);

        while (m.find()) {
            playerName = m.group(1);
        }
    }

    private void extractPlayerLevel() {
        String patternString = "<li.*>Level<h5.*>(\\d{1,2})</h5></li>";
        Pattern p = Pattern.compile(patternString);
        Matcher m = p.matcher(playerSwgohGgBody);

        while (m.find()) {
            level = Integer.parseInt(m.group(1));
        }
    }

    private void extractCharacters() {
        String splitOn = "<div class=\"col-xs-6 col-sm-3 col-md-3 col-lg-2\">";
        String[] parts = playerSwgohGgBody.split(splitOn);

        if (loggingOn) System.out.print("Starting character extract...");
        int i = 0;
        for(String part : parts) {
            String part1 = splitOn + part;
            String patternString = "<div class=\"col-xs-6 col-sm-3 col-md-3 col-lg-2\"><div.+?><div.+?><a.+?><img.+?><div.+?><div.+?></div><div.+?></div><div.+?></div><div.+?></div><div.+?></div><div.+?></div><div.+?></div><div.+?>\\d{1,2}</div><div.+?>[IVX]+</div></a></div><div.+?><a.+?>.+?</a></div></div></div>";
            Pattern p = Pattern.compile(patternString);
            Matcher m = p.matcher(part1);

            while (m.find()) {
                if (loggingOn) System.out.print(String.format("\rExtracting character %d ...", ++i));
                String characterData = m.group();
                final Character character = extractCharacter(characterData);
                characters.add(character);
                if (loggingOn) System.out.print(String.format("\rExtracted character %d.     ", i));
            }
        }

        if (loggingOn) System.out.println(String.format("\rExtracted %d characters.        ", i));
    }

    private Character extractCharacter(String data) {
        Character character = new Character();

        character.name = extractCharacterName(data);
        character.level = extractCharacterLevel(data);
        character.gearTier = extractGearTier(data);
        // character.power = -1;
        character.rarity = extractRarity(data);
        character.url = extractUrl(data);

        return character;
    }

    private String extractCharacterName(String data) {
        Pattern p = Pattern.compile("<a class=\"collection-char-name-link\".*?>(.*)</a>");
        Matcher m = p.matcher(data);

        if (m.find()) {
            return StringEscapeUtils.unescapeHtml4(m.group(1));
        }
        else {
            return null;
        }
    }

    private int extractCharacterLevel(String data) {
        Pattern p = Pattern.compile("<div class=\"char-portrait-full-level\">(\\d{1,2})</div>");
        Matcher m = p.matcher(data);

        if (m.find()) {
            return Integer.parseInt(StringEscapeUtils.unescapeHtml4(m.group(1)));
        }
        else {
            return -1;
        }
    }

    private int extractGearTier(String data) {
        Pattern p = Pattern.compile("<div class=\"char-portrait-full-gear-level\">([IXV]+)</div>");
        Matcher m = p.matcher(data);

        if (m.find()) {
            String t = StringEscapeUtils.unescapeHtml4(m.group(1));

            if (t.equals("I")) {
                return 1;
            } else if (t.equals("II")) {
                return 2;
            } else if (t.equals("III")) {
                return 3;
            } else if (t.equals("IV")) {
                return 4;
            } else if (t.equals("V")) {
                return 5;
            } else if (t.equals("VI")) {
                return 6;
            } else if (t.equals("VII")) {
                return 7;
            } else if (t.equals("VIII")) {
                return 8;
            } else if (t.equals("IX")) {
                return 9;
            } else if (t.equals("X")) {
                return 10;
            } else if (t.equals("XI")) {
                return 11;
            } else if (t.equals("XII")) {
                return 12;
            } else if (t.equals("XIII")) {
                return 13;
            } else {
                return -1;
            }
        }
        else {
            return -1;
        }
    }

    private String extractUrl(String data) {
        Pattern p = Pattern.compile("<a href=\"(.*?)\".*?>");
        Matcher m = p.matcher(data);

        if (m.find()) {
            return m.group(1);
        }
        else {
            return null;
        }
    }

    private int extractRarity(String data) {
        Pattern p = Pattern.compile("<div class=\"star star1(.*?)\"></div><div class=\"star star2(.*?)\"></div><div class=\"star star3(.*?)\"></div><div class=\"star star4(.*?)\"></div><div class=\"star star5(.*?)\"></div><div class=\"star star6(.*?)\"></div><div class=\"star star7(.*?)\"></div>");
        Matcher m = p.matcher(data);

        if (m.find()) {
            if (m.group(7).equals("")) {
                return 7;
            } else if (m.group(6).equals("")) {
                return 6;
            } else if (m.group(5).equals("")) {
                return 5;
            } else if (m.group(4).equals("")) {
                return 4;
            } else if (m.group(3).equals("")) {
                return 3;
            } else if (m.group(2).equals("")) {
                return 2;
            } else if (m.group(1).equals("")) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public void sendToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection;

        StringBuilder stringBuilder = getStringBuilderOutput();

        selection = new StringSelection(stringBuilder.toString());

        clipboard.setContents(selection, selection);

        System.out.println("** Data copied to clipboard. **");
    }

    private StringBuilder getStringBuilderOutput() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Character character : characters) {
            stringBuilder.append(String.format("%s\t%d\t%d\t%d\t%d\n", character.name, character.power, character.level, character.gearTier, character.rarity));
        }
        return stringBuilder;
    }

    public void writeOutputFile(String outputFilePath) {
        try {
            File outputFile = new File(outputFilePath);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

            try {
                for (Character character : characters) {
                    fileOutputStream.write(String.format("%s\t%d\t%d\t%d\t%d\n", character.name, character.power, character.level, character.gearTier, character.rarity).getBytes());
                }
            } finally {
                fileOutputStream.close();
            }
        } catch (Exception e) {
            System.out.println("Exception caught writing output file \"" + outputFilePath + "\"");
            e.printStackTrace();
            return;
        }
    }

    public void writeToConsole() {
        StringBuilder stringBuilder = getStringBuilderOutput();
        System.out.print(stringBuilder.toString());
    }

    private void getCharacterPower(Character character) {
        String url = "https://swgoh.gg" + character.url;

        String body = HttpUtility.performGet(url);

        character.power = getPower(body);
    }

    private int getPower(String body) {
        Pattern pattern = Pattern.compile("<span class=\"pc-stat-value\">(\\d+)</span><h5>Power</h5>");
        Matcher matcher = pattern.matcher(body);

        while (matcher.find()) {
            return Integer.parseInt(StringEscapeUtils.unescapeHtml4(matcher.group(1)));
        }

        return 0;
    }

    public static class Character {
        public String url;
        public String name;
        public int power;
        public int gearTier;
        public int level;
        public int rarity;

        @Override
        public String toString() {
            return String.format("Character: %s; %s / %s / %s / %s; %s", name, power, level, gearTier, rarity, url);
        }
    }
}
