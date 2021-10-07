package ai.makeitright.utilities.scraper;

import ai.makeitright.utilities.db.AuctionData;
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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;

import static ai.makeitright.utilities.db.AuctionData.crateAuctionDataObjectFromJSoupDocument;


public final class Scraper {

    public static ArrayList<AuctionData> scrape(final ArrayList<String> urlsOfAllAuctions) throws InterruptedException, IOException, ParseException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        System.out.println("------------------------------------------------------");
        System.out.println("Quantity of urls of all auctions: " + urlsOfAllAuctions.size());
        System.out.println("");
        ArrayList<AuctionData> auctionDatas = new ArrayList<>();
        for (String urlOfAuction : urlsOfAllAuctions) {
            System.out.println("Downloading url: " + urlOfAuction);
            Thread.sleep(500);

            CloseableHttpResponse responseFromSpecificAuction = getClosableHttpClient().execute(new HttpGet(urlOfAuction));
            HttpEntity httpEntity = responseFromSpecificAuction.getEntity();

            Document document = Jsoup.parse(EntityUtils.toString(httpEntity, "UTF-8"));
            AuctionData ad = crateAuctionDataObjectFromJSoupDocument(document, urlOfAuction);
            auctionDatas.add(ad);
        }
        return auctionDatas;
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