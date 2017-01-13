/**
 * Created by mwerner on 12/2/16.
 */
public class SwgohDataExtractor {

    public static void main(String[] args) throws Exception {
        {
            String swgohUsername;
            String outputFilePath = null;
            boolean shouldSendToClipboard = false;

            if (args.length < 1) {
                System.out.println("Error - missing swgoh.gg username input parameter");
            }
            swgohUsername = args[0];

            if (args.length > 1) {
                if (args[1].toUpperCase().equals("--OUTPUTFILE")) {
                    outputFilePath = args[2];
                } else if (args[1].toUpperCase().equals("--CLIPBOARD")) {
                    shouldSendToClipboard = true;
                }
            }

            SwgohDotGgDataExtractor extractor = new SwgohDotGgDataExtractor(swgohUsername, outputFilePath, shouldSendToClipboard);
            extractor.process();
        }
    }
}
