package w.whatever.data.jpa.jobs.ocl.scraper.data;

import org.junit.Test;
import org.w3c.tidy.Tidy;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rich on 11/24/17.
 */
public class OclScraperGameTest {

    /*


    <tr id="plyr2898" class="pncPlayerRow playerTableBgRow0"><td class="playertablePlayerName" id="playername_2898" style="">Drew Bennett, Ten&nbsp;WR</td><td><div><a href content="ajax#/ffl/format/pvopop/summary?leagueId=35734&positionId=3&playerId=70320&seasonId=2006" class="flexpop" instance="_ppc">NYJ</a></div></td><td class="gameStatusDiv"><span class="gameNotch_260910010_10 onFieldNotch">&raquo;</span> <a class="gamestatus_260910010_10" href="http://www.espn.com/nfl/game?gameId=0" target="_blank">L 16-23</a></td><td class="playertableStat appliedPoints">14</td></tr>


     */

    // private final static String playerGameTemplate = "<tr id=\"(.*)\" class=\"pncPlayerRow playerTableBgRow0\"><td class=\"playertablePlayerName\" id=\"(.*)\" style=\"\">(.*), (.*)&nbsp;(.*)</td><td><div><a href content=\"(.*)\" class=\"flexpop\" instance=\"_ppc\">(.*)</a></div></td><td class=\"gameStatusDiv\"><span class=\"(.*)\">&raquo;</span> <a class=\"(.*)\" href=\"http://www.espn.com/nfl/game?gameId=0\" target=\"_blank\">(.*)</a></td><td class=\"playertableStat appliedPoints\">([0-9]+)</td></tr>";
    private final static String playerGameTemplate = "<tr id=\"(plyr[0-9]+)\" class=\"pncPlayerRow playerTableBgRow0\"><td class=\"playertablePlayerName\" id=\"playername_[0-9]+\" style=\"\">([a-zA-Z\\s]+), (.*)&nbsp;([A-Z]{2,3})</td>(.*)</tr>";

    @Test
    public void runTest() {
        try {
            String contents = new String(Files.readAllBytes(Paths.get(new OclScraperGame(6, 11, 2017).getGameHtmlFile().getCanonicalPath())));
            System.out.println(contents);
            Pattern pattern = Pattern.compile(playerGameTemplate);
            Matcher matcher = pattern.matcher(contents);
            int i = 0;
            while (matcher.find()) {
                System.out.println(matcher.group(1));
                System.out.println(matcher.group(2));
                i++;
            }
            System.out.println(String.format("%d matches...", i));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void runTest2() {
        try {
            String w = new OclScraperGame(6, 11, 2017).writeXHTMLFromHTML();
            System.out.println(w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void runTest3() {
        try {
            String w = new OclScraperGame(6, 11, 2017).writeXMLFromXHTML();
            System.out.println(w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getXHTMLFromHTML(String inputFile, String outputFile) throws Exception {

        File file = new File(inputFile);
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            fos = new FileOutputStream(outputFile);
            is = new FileInputStream(file);
            Tidy tidy = new Tidy();
            tidy.setInputEncoding("UTF-8");
            tidy.setOutputEncoding("UTF-8");
            // tidy.setPrintBodyOnly(true); // only print the content
            tidy.setXmlOut(true); // to XML
            tidy.setSmartIndent(true);
            tidy.setMakeClean(true);
            tidy.setShowErrors(0);
            tidy.setQuoteNbsp(false);
            tidy.setForceOutput(true);
            tidy.parseDOM(is, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    fos = null;
                }
                fos = null;
            }
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    is = null;
                }
                is = null;
            }
        }

        return outputFile;
    }

    @Test
    public void sydneyTest() {

        List<Integer> numbers = Arrays.asList(4, 2, 2, 4, 44, 89, 123456);

        for (Integer number : numbers) {
            System.out.println(String.format("I am %d years old", number));
        }
    }
}
