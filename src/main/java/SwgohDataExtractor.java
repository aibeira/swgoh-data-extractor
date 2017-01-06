/**
 * Created by mwerner on 12/2/16.
 */
public class SwgohDataExtractor {

    public static void main(String[] args) throws Exception {
        {
            SwgohDotGgDataExtractor extractor = new SwgohDotGgDataExtractor(args[0]);
            extractor.process();
        }
    }
}
