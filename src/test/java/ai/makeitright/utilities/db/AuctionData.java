package ai.makeitright.utilities.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "auctions")
public class AuctionData {
    @DatabaseField(id = true)
    private String id;
    @DatabaseField
    private String marka;
    @DatabaseField
    private String model;
    @DatabaseField
    private String rokProdukcji;
    @DatabaseField
    private String numerRejestracyjny;
    @DatabaseField
    private String vin;
    @DatabaseField
    private String rodzajPaliwa;
    @DatabaseField
    private String klasaEuro;
    @DatabaseField
    private String kluczyki;
    @DatabaseField
    private String dowodRejestracyjny;
    @DatabaseField
    private String kartaPojazdu;
    @DatabaseField
    private Long przebieg;
    @DatabaseField
    private Long cena;
    @DatabaseField
    private String pdfUrl;
    @DatabaseField
    private String wyposazenie;
    @DatabaseField
    private Timestamp dataWyszukania;
    @DatabaseField
    private Timestamp doKoncaAukcji;
    @DatabaseField
    private String zrodlo;
    @DatabaseField
    private String typAukcji;

    public static AuctionData crateAuctionDataObjectFromJSoupDocument(Document document, String urlOfAuction) throws ParseException {
        Element divAC = document.selectFirst("#body");
        AuctionData ad = new AuctionData();
        ad.setId(urlOfAuction);

        if (divAC.selectFirst("div.product-details td:contains(Marka, Model)") != null) {
            ad.setMarka(divAC.selectFirst("div.product-details td:contains(Marka, Model)").nextElementSibling().ownText());
            ad.setModel(divAC.selectFirst("div.product-details td:contains(Marka, Model)").nextElementSibling().ownText());
        }

        if (divAC.selectFirst("div.product-details td:contains(Rok produkcji:)") != null) {
            ad.setRokProdukcji(divAC.selectFirst("div.product-details td:contains(Rok produkcji:)").nextElementSibling().ownText());
        }

        if (divAC.selectFirst("div.product-details td:contains(Nr rejestracyjny:)") != null) {
            ad.setNumerRejestracyjny(divAC.selectFirst("div.product-details td:contains(Nr rejestracyjny:)").nextElementSibling().ownText());
        } else {
            ad.setNumerRejestracyjny("");
        }

        if (divAC.selectFirst("div.product-details td:contains(VIN:)") != null) {
            ad.setVin(divAC.selectFirst("div.product-details td:contains(VIN:)").nextElementSibling().ownText());
        }

        if (divAC.selectFirst("div.product-details td:contains(Rodzaj paliwa:)") != null) {
            ad.setRodzajPaliwa(divAC.selectFirst("div.product-details td:contains(Rodzaj paliwa:)").nextElementSibling().ownText());
        } else {
            ad.setRodzajPaliwa("");
        }

        ad.setKlasaEuro("");

        ad.setKluczyki("");

        ad.setDowodRejestracyjny("");

        ad.setKartaPojazdu("");

        if (divAC.selectFirst("div.product-details td:contains(Przebieg odczytany:)") != null) {
            String przebiegWithKm = divAC.selectFirst("div.product-details td:contains(Przebieg odczytany:)").nextElementSibling().ownText();
            String przebieg = przebiegWithKm.replace("km", "");
            if (!przebieg.equals("-")) {
                ad.setPrzebieg(Long.valueOf(przebieg));
            } else {
                ad.setPrzebieg(Long.valueOf(0));
            }
        } else {
            ad.setPrzebieg(Long.valueOf(0));
        }

        String cenaFromPage = divAC.selectFirst("span.auction-price").ownText();
        String cena = cenaFromPage.replaceAll("\\s", "").substring(0, cenaFromPage.indexOf(",") - 1);
        ad.setCena(Long.valueOf(cena));

        ad.setPdfUrl("");

        if (divAC.selectFirst("#details-panel div[style]:contains(enie dodatkowe:)") != null) {
            ad.setWyposazenie(divAC.selectFirst("#details-panel div[style]:contains(Wyposa)").nextElementSibling().ownText() + " " +
                    divAC.selectFirst("#details-panel div[style]:contains(enie dodatkowe:)").nextElementSibling().ownText());
        } else {
            if (divAC.selectFirst("#details-panel div[style]:contains(Wyposa)") != null) {
                ad.setWyposazenie(divAC.selectFirst("#details-panel div[style]:contains(Wyposa)").nextElementSibling().ownText());
            }
        }

        Timestamp dataWyszukaniaTimestamp = new Timestamp(System.currentTimeMillis());
        ad.setDataWyszukania(dataWyszukaniaTimestamp);

        if (divAC.selectFirst("#time-to-end") != null) {
            String doKoncaAukcjiWithGodzinaWord = divAC.selectFirst("#time-to-end").ownText();
            String doKoncaAukcji = doKoncaAukcjiWithGodzinaWord.replace("godzina ", "");
            Pattern pattern1 = Pattern.compile("(\\d{2}).(\\d{2}).(\\d{4})");
            Pattern pattern2 = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");
            Matcher matcher1 = pattern1.matcher(doKoncaAukcji);
            Matcher matcher2 = pattern2.matcher(doKoncaAukcji);
            String dataddMMyyyy;
            String godzina;
            String datayyyyMMdd;
            if (matcher1.find() && matcher2.find()) {
                dataddMMyyyy = matcher1.group();
                godzina = matcher2.group();
                String[] x = dataddMMyyyy.split("\\.");
                String dd = (String) Array.get(x, 0);
                String mM = (String) Array.get(x, 1);
                String yyyy = (String) Array.get(x, 2);
                datayyyyMMdd = yyyy + "-" + mM + "-" + dd;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date parsedDate = dateFormat.parse(datayyyyMMdd + " " + godzina);
                Timestamp koniecAukcjiTimestamp = new Timestamp(parsedDate.getTime());
                ad.setDoKoncaAukcji(koniecAukcjiTimestamp);
            } else {
                ad.setDoKoncaAukcji(new Timestamp(0L));
            }
            ad.setZrodlo(System.getProperty("inputParameters.title"));
            if (!document.select("#kup-teraz").isEmpty()) {
                ad.setTypAukcji("kup teraz");
            } else if (!document.select("#licytuj").isEmpty()) {
                ad.setTypAukcji("licytacja");
            }
        }
        return ad;
    }
}