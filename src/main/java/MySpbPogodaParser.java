import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySpbPogodaParser {
    static String url="https://pogoda.spb.ru";
    static Pattern patternForDate=Pattern.compile("\\d{2}\\.\\d{2}");
    static boolean locker=false;

    private static Document getPage() throws IOException {
        Document page = Jsoup.parse(new URL(url), 3000);
        return page;
    }

    private static String getStringfromDate(String stringDate) throws Exception {
        Matcher matcher = patternForDate.matcher(stringDate);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new Exception("Can not format string to date");
    }


    public static void main(String[] args) throws Exception {

        Document page=getPage();
        Element tableWth = page.select("table[class=wt]").first();
        Elements hight = tableWth.select("tr[class=wth]");
        Elements values = tableWth.select("tr[valign=top]");

        Queue<String> queue=new PriorityQueue<>();
        Map<String,Map<String,String[]>> pogodaMap=new TreeMap<>();
        Map<String, String[]> mapDay = new TreeMap<>();

        for (Element hightName : hight) {
            String date = getStringfromDate(hightName.select("th[id=dt]").text());
            queue.add(date);
        }

        for (Element valuesName : values) {

            if (values.get(0).text().contains("Ночь") && !locker) {locker=true; continue;}
            Elements el=valuesName.select("td");

            if (el.get(0).text().contains("Ночь")) {
                mapDay.put(el.get(0).text(),new String[]{el.get(1).text(),el.get(2).text(),el.get(5).text()});
                pogodaMap.put(queue.poll(), new TreeMap<>(mapDay) );
                mapDay.clear();
            }
            mapDay.put(el.get(0).text(),new String[]{el.get(1).text(),el.get(2).text(),el.get(5).text()});
        }

        for (Map.Entry<String, Map<String, String[]>> entry:pogodaMap.entrySet()) {
            System.out.println("Дата: " + entry.getKey());
            System.out.println("Утро");
            System.out.println(Arrays.toString(entry.getValue().get("Утро")));
            System.out.println("День");
            System.out.println(Arrays.toString(entry.getValue().get("День")));
            System.out.println("Вечер");
            System.out.println(Arrays.toString(entry.getValue().get("Вечер")));
            System.out.println("Ночь");
            System.out.println(Arrays.toString(entry.getValue().get("Ночь")));
            System.out.println("******************************************************");
        }

    }
}
