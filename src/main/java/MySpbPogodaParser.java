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
    private String url = "https://pogoda.spb.ru";
    // regExp = 01.01
    private final Pattern patternForDate = Pattern.compile("\\d{2}\\.\\d{2}");

     // the first night must be skipped

    private boolean locker = false;

    private final Queue<String> queueDate = new PriorityQueue<>();
    /**
     * key - name day
     * value - data day
     */
    private final Map<String, String[]> mapDay = new TreeMap<>();
    /**
     * Key - Date from future queueDate
     * Value - mapDay
     */
    private final Map<String, Map<String, String[]>> WeatherMap = new TreeMap<>();


    // Помещаем в документ содержимое ссылки, которая разбивает и группирует его элементы
    // при помощи класса Jsoup
    private Document getPage() {
        Document page = null;
        try {
            page = Jsoup.parse(new URL(url), 3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return page;
    }

    // Ищем на странице совпадение по паттерну
    private String getStringfromDate(String stringDate) throws Exception {
        Matcher matcher = patternForDate.matcher(stringDate);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new Exception("Can not format string to date");
    }

    public void runApi() {
        Document page = getPage();
        Element tableWth = page.select("table[class=wt]").first();
        Elements hight = tableWth.select("tr[class=wth]");
        Elements values = tableWth.select("tr[valign=top]");

        fillDateQueue(hight);
        fillWeatherMap(values);
        printWeekDayWeather(WeatherMap);

    }

    private void fillDateQueue(Elements hight) {
        try {
            for (Element hightName : hight) {
                String date = getStringfromDate(hightName.select("th[id=dt]").text());
                queueDate.add(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillWeatherMap(Elements values) {
        for (Element valuesName : values) {
            // пропускаем первую ночь, эта разовая проверка
            if (values.get(0).text().contains("Ночь") && !locker) {
                locker = true;
                continue;
            }
            // собираем данные по столбцам строки
            Elements el = valuesName.select("td");

            if (el.get(0).text().contains("Ночь")) {
                mapDay.put(el.get(0).text(), new String[]{el.get(1).text(), el.get(2).text(), el.get(5).text()});
                WeatherMap.put(queueDate.poll(), new TreeMap<>(mapDay));
                mapDay.clear();
            }
            mapDay.put(el.get(0).text(), new String[]{el.get(1).text(), el.get(2).text(), el.get(5).text()});
        }
    }

    private void printWeekDayWeather(Map<String, Map<String, String[]>> weatherMap) {
        for (Map.Entry<String, Map<String, String[]>> entry : weatherMap.entrySet()) {
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
