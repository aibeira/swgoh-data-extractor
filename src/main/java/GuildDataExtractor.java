import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mwerner on 1/19/17.
 */
public class GuildDataExtractor {
    private List<Member> members;
    private String guildNumber;
    private String guildProfileBody;
    private String name;
    private boolean loggingOn;

    public GuildDataExtractor(String guildNumber, boolean loggingOn) {
        this.guildNumber = guildNumber;
        members = new ArrayList<Member>();
        this.loggingOn = loggingOn;

        guildProfileBody = getGuildProfile();
        extractGuildName();
        extractMembers();
    }

    public void getMemberData() {
        if (loggingOn) System.out.print("Fetching data for guild members");
        int i = 0;
        for(Member member : members) {
            if (loggingOn) System.out.print(String.format("\rFetching data for ( %3d / %3d ) %-50s", ++i, members.size(), member.playerName));
            PlayerDataExtractor playerDataExtractor = new PlayerDataExtractor(member.swgohGgUsername, loggingOn);
            member.characters = playerDataExtractor.getCharacters();
            member.playerLevel = playerDataExtractor.getLevel();
            for(PlayerDataExtractor.Character character : member.characters) {
                if (character.rarity == 7) {
                    member.sevenStarCharacters++;
                } else if (character.rarity == 6) {
                    member.sixStarCharacters++;
                }

                if (character.gearTier == 11) {
                    member.gear11Characters++;
                } else if (character.gearTier == 10) {
                    member.gear10Characters++;
                } else if (character.gearTier == 9) {
                    member.gear9Characters++;
                }
            }
        }
        System.out.println(String.format("\rFetched data for %d members.", i));
    }

    public String getName() {
        return name;
    }

    public List<Member> getMembers() {
        return members;
    }

    private String getGuildProfile() {
        String url = "https://swgoh.gg/g/" + guildNumber + "/nonsense/";

        String body = HttpUtility.performGet(url);

        return body;
    }

    private void extractGuildName() {
        String patternString = "<h1.*>([\\w ]+)</h1>";
        Pattern p = Pattern.compile(patternString);
        Matcher m = p.matcher(guildProfileBody);

        while (m.find()) {
            name = m.group(1);
        }

        System.out.println("Guild name: \"" + name + "\"");
    }

    private List<Member> extractMembers() {
        String patternString = "<a href=\"/u/([\\w\\- ]+)/\">([\\w\\- ]+)</a>";
        Pattern p = Pattern.compile(patternString);
        Matcher m = p.matcher(guildProfileBody);

        while (m.find()) {
            Member member = new Member();
            member.swgohGgUsername = m.group(1);
            member.playerName = m.group(2);
            members.add(member);
            if (loggingOn) System.out.println("Found member: " + member.swgohGgUsername + "\t" + member.playerName);
        }

        return members;
    }

    public void sendToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection;

        StringBuilder[] stringBuilder = getStringBuilderOutput();

        selection = new StringSelection(stringBuilder[0].toString() + "\n\n" + stringBuilder[1].toString());

        clipboard.setContents(selection, selection);

        System.out.println("** Data copied to clipboard. **");
    }

    private StringBuilder[] getStringBuilderOutput() {
        StringBuilder memberData = new StringBuilder();
        StringBuilder memberCharacterData = new StringBuilder();
        for (Member member : members) {
            memberData.append(String.format("%s\t%d\t%d\t%d\t%d\t%d\t%d\n", member.playerName, member.playerLevel, member.sevenStarCharacters, member.sixStarCharacters, member.gear11Characters, member.gear10Characters, member.gear9Characters));
            for (PlayerDataExtractor.Character character : member.characters) {
                memberCharacterData.append(String.format("%s\t%s\t%d\t%d\t%d\n", member.playerName, character.name, character.level, character.rarity, character.gearTier));
            }
        }
        return new StringBuilder[] { memberData, memberCharacterData };
    }

    private void writeStringBuilderToFile(StringBuilder stringBuilder, String outputFilePath) {
        try {
            File outputFile = new File(outputFilePath);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

            try {
                fileOutputStream.write(stringBuilder.toString().getBytes());
            } finally {
                fileOutputStream.close();
            }
        } catch (Exception e) {
            System.out.println("Exception caught writing output file \"" + outputFilePath + "\"");
            e.printStackTrace();
            return;
        }
    }

    public void writeOutputFile(String outputFilePathBase) {
        final StringBuilder[] stringBuilderOutput = getStringBuilderOutput();

        writeStringBuilderToFile(stringBuilderOutput[0], outputFilePathBase + ".members");
        writeStringBuilderToFile(stringBuilderOutput[1], outputFilePathBase + ".characters");
    }

    public void writeToConsole() {
        StringBuilder[] stringBuilder = getStringBuilderOutput();
        System.out.print(stringBuilder[0].toString());
        System.out.print(stringBuilder[1].toString());
    }

    public static class Member {
        public String swgohGgUsername;
        public String playerName;
        public int playerLevel;
        public int sevenStarCharacters;
        public int sixStarCharacters;
        public int gear11Characters;
        public int gear10Characters;
        public int gear9Characters;
        public List<PlayerDataExtractor.Character> characters;

        public Member() {
            this.sixStarCharacters = 0;
            this.sevenStarCharacters = 0;
            this.gear9Characters = 0;
            this.gear10Characters = 0;
            this.gear11Characters = 0;
        }
    }
}
