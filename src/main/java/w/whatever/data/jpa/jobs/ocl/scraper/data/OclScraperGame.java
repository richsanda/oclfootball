package w.whatever.data.jpa.jobs.ocl.scraper.data;

import com.google.common.collect.Maps;
import org.w3c.tidy.Tidy;
import w.whatever.data.jpa.jobs.ocl.scraper.util.Transforms;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by rich on 11/24/17.
 */
public class OclScraperGame {

    private final Integer team;
    private final Integer scoringPeriod;
    private final Integer season;

    private String htmlContent;

    private final static String gameUrlTemplate = "http://games.espn.com/ffl/boxscorequick?leagueId=35734&teamId=%d&scoringPeriodId=%d&seasonId=%d&view=scoringperiod&version=quick";
    private final static String gameHtmlFileTemplate = "html/%d.%d.%d.html";
    private final static String gameXhtmlFileTemplate = "xhtml/%d.%d.%d.xhtml";
    private final static String gameXmlFileTemplate = "%d.%d.%d.xml";
    private final static String gamesDir = "/Users/rich/checkouts/ocl/src/main/resources/games";

    private final static String transformLocation = "/xsl/xhtmlToXml.xslt";

    public OclScraperGame(Integer team, Integer scoringPeriod, Integer season) {
        this.team = team;
        this.scoringPeriod = scoringPeriod;
        this.season = season;
    }

    public String getGameUrl() {
        return String.format(gameUrlTemplate, this.team, scoringPeriod, season);
    }

    public String getGameHtmlFileTemplate() {
        return String.format(gameHtmlFileTemplate, this.season, scoringPeriod, team);
    }

    public File getGameHtmlFile() {
        return new File(gamesDir, getGameHtmlFileTemplate());
    }

    public String getGameXhtmlFileTemplate() {
        return String.format(gameXhtmlFileTemplate, this.season, scoringPeriod, team);
    }

    public File getGameXhtmlFile() {
        return new File(gamesDir, getGameXhtmlFileTemplate());
    }

    public String getGameXmlFileTemplate() {
        return String.format(gameXmlFileTemplate, this.season, scoringPeriod, team);
    }

    public File getGameXmlFile() {
        return new File(gamesDir, getGameXmlFileTemplate());
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public void writeHtmlToResource() throws FileNotFoundException, IOException {
        File file = getGameHtmlFile();
        PrintWriter writer = new PrintWriter(file);
        writer.write(htmlContent);
        writer.close();
        System.out.println(file.getCanonicalPath());
    }

    /*

    team_id = 1
    scoring_period_id = 1
    season_id = 2015

    writeDir = "/Users/richsanda/life/python/crawlers/espn/games/%d.%d.%d.html"
    writeFile = None
    gameURL = "http://games.espn.go.com/ffl/boxscorequick?leagueId=35734&teamId=%d&scoringPeriodId=%d&seasonId=%d&view=scoringperiod&version=quick"

     */

    public void getGameFromEspn() throws IOException {
        URL gameUrl = new URL(getGameUrl());
        HttpURLConnection connection = (HttpURLConnection)gameUrl.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        // connection.setInstanceFollowRedirects(true);
        System.out.println(String.valueOf(connection.getResponseCode()));
        String body = readInputStreamToString(connection);
        System.out.println(body);
        setHtmlContent(body);
    }

    private String readInputStreamToString(HttpURLConnection connection) {
        String result = null;
        StringBuffer sb = new StringBuffer();
        InputStream is = null;

        try {
            is = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public String writeXHTMLFromHTML() throws Exception {

        File file = getGameHtmlFile();
        FileOutputStream fos = null;
        InputStream is = null;

        String outputFile = getGameXhtmlFile().getCanonicalPath();

        try {
            fos = new FileOutputStream(outputFile);
            is = new FileInputStream(file);
            Tidy tidy = new Tidy();
            tidy.setInputEncoding("UTF-8");
            tidy.setOutputEncoding("UTF-8");
            // tidy.setPrintBodyOnly(true); // only print the content
            tidy.setXmlOut(true); // to XML
            // tidy.setSmartIndent(true);
            // tidy.setMakeClean(true);
            tidy.setShowErrors(0);
            tidy.setDocType("omit");
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

    public String writeXMLFromXHTML() throws IOException {

        Transforms.transform(new StreamSource(getGameXhtmlFile()), new StreamResult(getGameXmlFile()), transformLocation, makeGameKeyParams());

        return getGameXmlFile().getCanonicalPath();
    }

    private Map<String, String> makeGameKeyParams() {
        Map<String, String> result = Maps.newHashMap();
        result.put("season", season.toString());
        result.put("scoringPeriod", scoringPeriod.toString());
        result.put("team", team.toString());
        return result;
    }
}
