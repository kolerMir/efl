package ai.makeitright.utilities.crawler;

import com.google.common.primitives.Ints;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.apache.commons.lang3.math.NumberUtils.max;

public final class Crawler {
    static Logger logger = LoggerFactory.getLogger(Crawler.class);


    public static ArrayList<String> crawl() throws IOException, InterruptedException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException {
        System.setProperty("jsse.enableSNIExtension", "false");
        String uriString = System.getProperty("inputParameters.startPage");
        URI uRI = new URI(uriString);
        CloseableHttpResponse response0 = getClosableHttpClient().execute(new HttpGet(uRI.toString()));
        HttpEntity entity = response0.getEntity();
        logger.atInfo().log("Status of GET to: " + uRI.toString() + " is " + response0.getStatusLine());

        Document parse = Jsoup.parse(EntityUtils.toString(entity, "UTF-8"));
        String aInService= "div.AuctionTitle > a";
        Elements asFrom1stPage = parse.select(aInService);
        Elements aSoFPaging = parse.select("div.pager li a[href]");
        ArrayList<Integer> numbers = new ArrayList<>();
        for (Element a : aSoFPaging) {
            String textOfA = a.text();
            if (textOfA.matches("^[0-9]+$")) {
                numbers.add(Integer.parseInt(textOfA));
            }
        }
        int[] ints = Ints.toArray(numbers);
        int maxNumberOfPageInPaging = max(ints) - 1;
        ArrayList<String> urlsOfSpecificAuctions = new ArrayList<>();
        ArrayList<String> hrefsOfAllAuctions = new ArrayList<>();
        for (Element a : asFrom1stPage) {
            hrefsOfAllAuctions.add(a.attr("href"));
        }
        for (int i = 1; i <= maxNumberOfPageInPaging; i++) {
            Thread.sleep(300);
            String uriForHttpGet = "https://aukcje.efl.com.pl/AuctionList?page=" + i + "&sort=Title-asc&fc=3";
            logger.atInfo().log("Going to: " + uriForHttpGet);
            CloseableHttpResponse response1 = getClosableHttpClient().execute(new HttpGet(uriForHttpGet));
            logger.atInfo().log("Getting href values for auctions from " + i + " page of search results");
            HttpEntity httpEntity = response1.getEntity();
            Document document = Jsoup.parse(EntityUtils.toString(httpEntity, "UTF-8"));
            Elements asFrom2ndPageAndSoOn = document.select(aInService);
            for (Element a : asFrom2ndPageAndSoOn) {
                hrefsOfAllAuctions.add(a.attr("href"));
            }
        }
        for (String hrefOfAuction : hrefsOfAllAuctions) {
            urlsOfSpecificAuctions.add("https://aukcje.efl.com.pl" + hrefOfAuction);
        }
        return urlsOfSpecificAuctions;
    }

    private static CloseableHttpClient getClosableHttpClient() throws
            KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        BasicCookieStore cookieStore = new BasicCookieStore();
        return HttpClients.custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setDefaultCookieStore(cookieStore)
                .build();
    }
}