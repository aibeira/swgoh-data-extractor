import java.net.URLEncoder;
import java.util.Scanner;

/**
 * Created by mwerner on 12/2/16.
 */
public class SwgohDataExtractor {

    static OperationMode operationMode = OperationMode.Unknown;
    static boolean shouldSendToClipboard = false;
    static String outputFilePath = null;
    static String username = null;
    static String guildNumber = null;
    static boolean loggingOn = false;
    static int cSeq = 1;

    public static void main(String[] args) throws Exception {
        if (!parseArguments(args)) {
            return;
        }
        switch (operationMode) {
            case UserExtract:
                PlayerDataExtractor playerDataExtractor = new PlayerDataExtractor(username, loggingOn);
                playerDataExtractor.getPowers();
                if (shouldSendToClipboard) {
                    playerDataExtractor.sendToClipboard();
                } else if (outputFilePath != null) {
                    playerDataExtractor.writeOutputFile(outputFilePath);
                } else {
                    playerDataExtractor.writeToConsole();
                }
                return;
            case GuildExtract:
                GuildDataExtractor guildDataExtractor = new GuildDataExtractor(guildNumber, loggingOn);
                guildDataExtractor.getMemberData();
                if (shouldSendToClipboard) {
                    guildDataExtractor.sendToClipboard();
                } else if (outputFilePath != null) {
                    guildDataExtractor.writeOutputFile(outputFilePath);
                } else {
                    guildDataExtractor.writeToConsole();
                }
                return;
            case Test:
                Scanner keyboard = new Scanner(System.in);
                String input = "";

                while (!input.toUpperCase().equals("X")) {
                    if (input.toUpperCase().equals("H")) {
                        // send idle
                        makeCallDirectorSendMockNotify(PresentityState.HungUp);
                    } else if (input.toUpperCase().equals("R")) {
                        // send ringing
                        makeCallDirectorSendMockNotify(PresentityState.Ringing);
                    } else if (input.toUpperCase().equals("A")) {
                        // send busy
                        makeCallDirectorSendMockNotify(PresentityState.Answered);
                    } else if (input.toUpperCase().equals("B")) {
                        // send busy
                        makeCallDirectorSendMockNotify(PresentityState.Busy);
                    }
                    System.out.println("(H)angup, (R)inging, (A)nswered, (B)usy, E(x)it");
                    input = keyboard.next();
                }

                return;
            case Unknown:
            default:
                System.out.println("Unimplemented OperationMode \"" + operationMode.toString() + "\"");
                return;
        }
    }

    private static void makeCallDirectorSendMockNotify(PresentityState presentityState) {
        String mocknotify = "1";
        String notifyUrl = "sip:mrw_test_3_2000@10.4.3.15:5065";
        String contentType = "";
        String dialogId = "dialogId";
        String dialogElement = "";
        String content = "";
        String event = "";

        switch (presentityState) {
            case Ringing:
                dialogElement = "<dialog id=\"" + dialogId + "\">" +
                        "<state>early</state>" +
                        "</dialog>";
                content = "<?xml version=\"1.0\"?>\n<dialog-info xmlns=\"urn:ietf:params:xml:ns:dialog-info\" version=\"0\" state=\"full\" entity=\"sip:2001@n4wStoLmRxqaqOVvA6zCeA\">" + dialogElement + "\n</dialog-info>\n";
                contentType = "application/dialog-info+xml";
                event = "dialog";
                break;
            case Answered:
                dialogElement = "<dialog id=\"" + dialogId + "\">" +
                        "<state>confirmed</state>" +
                        "</dialog>";
                content = "<?xml version=\"1.0\"?>\n<dialog-info xmlns=\"urn:ietf:params:xml:ns:dialog-info\" version=\"0\" state=\"full\" entity=\"sip:2001@n4wStoLmRxqaqOVvA6zCeA\">" + dialogElement + "\n</dialog-info>\n";
                contentType = "application/dialog-info+xml";
                event = "dialog";
                break;
            case HungUp:
                dialogElement = "<dialog id=\"" + dialogId + "\">" +
                        "<state>terminated</state>" +
                        "</dialog>";
                content = "<?xml version=\"1.0\"?>\n<dialog-info xmlns=\"urn:ietf:params:xml:ns:dialog-info\" version=\"0\" state=\"full\" entity=\"sip:2001@n4wStoLmRxqaqOVvA6zCeA\">" + dialogElement + "\n</dialog-info>\n";
                contentType = "application/dialog-info+xml";
                event = "dialog";
                break;
            case Busy:
                {
                    StringBuilder c = new StringBuilder();

                    c.append("<?xml version=\"1.0\"?>\n");
                    c.append("<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" entity=\"sip:2001@n4wStoLmRxqaqOVvA6zCeA\">\n");
                    c.append("<tuple>\n");
                    c.append("<status>\n");
                    c.append("<basic>\n");
                    c.append("closed");
                    c.append("</basic>\n");
                    c.append("</status>\n");
                    c.append("</tuple>\n");
                    c.append("</presence>\n");
                    c.append("");
                    content = c.toString();
                }
//                content = "<dialog-info xmlns=\"urn:ietf:params:xml:ns:dialog-info\" version=\"0\" state=\"full\" entity=\"sip:2001@n4wStoLmRxqaqOVvA6zCeA\">" + dialogElement + "\n</dialog-info>\n";
                contentType = "application/pidf+xml";
                event = "presence";
                break;
            default:
            case Idle:
                break;
        }
        String callId = "6bb33ac-6a95bcbe@10.4.3.15";
        String fromUri = "sip:mrw_test_3_2000@sacq2-cpcd-1.corp.alianza.com";
        String fromTag = "dee576d4dc0813b6";
        String toUri = "sip:2001@n4wStoLmRxqaqOVvA6zCeA";
        String toTag = "";
        String cSeqHeaderValue = String.valueOf(++cSeq);
        String subscriptionActive = "true";

        String url = "http://sacq2-cpcd-1.corp.alianza.com:8085/?" +
                "notifyUrl=" + URLEncoder.encode(notifyUrl) + "&" +
                "mockNotify=" + URLEncoder.encode(mocknotify) + "&" +
                "contentType=" + URLEncoder.encode(contentType) + "&" +
                "content=" + URLEncoder.encode(content) + "&" +
                "callId=" + URLEncoder.encode(callId) + "&" +
                "fromUri=" + URLEncoder.encode(fromUri) + "&" +
                "fromTag=" + URLEncoder.encode(fromTag) + "&" +
                "toUri=" + URLEncoder.encode(toUri) + "&" +
                "toTag=" + URLEncoder.encode(toTag) + "&" +
                "cSeq=" + URLEncoder.encode(cSeqHeaderValue) + "&" +
                "subscriptionActive=" + URLEncoder.encode(subscriptionActive) + "&" +
                "eventHeader=" + URLEncoder.encode(event) +
                "";

        System.out.println(url);

        final String output = HttpUtility.performGet(url);

        System.out.println(output);
    }

    private static boolean parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].toUpperCase().equals("--USER")) {
                operationMode = OperationMode.UserExtract;
                username = args[++i];
            } else if (args[i].toUpperCase().equals("--GUILD")) {
                operationMode = OperationMode.GuildExtract;
                guildNumber = args[++i];
            } else if (args[i].toUpperCase().equals("--TEST")) {
                operationMode = OperationMode.Test;
            } else if (args[i].toUpperCase().equals("--CLIPBOARD")) {
                shouldSendToClipboard = true;
            } else if (args[i].toUpperCase().equals("--LOGGINGON")) {
                loggingOn = true;
            } else if (args[i].toUpperCase().equals("--OUTPUTFILE")) {
                outputFilePath = args[++i];
            }
        }

        if (operationMode == OperationMode.Unknown) {
            System.out.println("Missing mode");
            return false;
        }

        if (operationMode == OperationMode.UserExtract && username == null) {
            System.out.println("Missing username");
            return false;
        }

        if (operationMode == OperationMode.GuildExtract && guildNumber == null) {
            System.out.println("Missing guild number");
            return false;
        }

        return true;
    }

    enum OperationMode {
        Unknown,
        UserExtract,
        GuildExtract,
        Test;
    }

    enum PresentityState {
        Idle,
        Ringing,
        Answered,
        HungUp,
        Busy
    }
}
