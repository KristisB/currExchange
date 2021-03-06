package study.currencyrates;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resources;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

//import static jdk.xml.internal.SecuritySupport.getResourceAsStream;

@Controller
public class CurrCalc {
    Database db = new Database("heroku_9dea6c2af45e4d4"); //local: "currdb";

    @RequestMapping("/home")
    public @ResponseBody
    String home() {
        return getTextFromFile("home.html");
    }

    @RequestMapping("/exchange")
    public @ResponseBody
    String exchange(
            @RequestParam("curr1") String curr1,
            @RequestParam("curr2") String curr2,
            @RequestParam("amount") double amount1,
            @RequestParam("fxdate") String fxDate,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        String log = ip + " requested to calculate rate " + amount1 + " " + curr1 + " into " + curr2+" on date "+fxDate;
        System.out.println(log);
        db.addLog(log);
        double rate1 = db.getRate(curr1,fxDate);
        if (rate1 == 0) {
            return "no such currency in database " + curr1+" at date "+ fxDate;
        }
        double rate2 = db.getRate(curr2,fxDate);
        if (rate1 == 0) {
            return "no such currency in database " + curr2+" at date "+ fxDate;
        }
        double result = amount1 / rate1 * rate2;
        return amount1 + " " + curr1 + " exchanged to " + curr2 + " on "+fxDate+" rate equals: " + result + " " + curr2;
    }

    @RequestMapping("/load_fxrates")
    public @ResponseBody
    String loadFxRates(
            @RequestParam("date") String date,
            HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String log = ip + " requested to load fx rates from LB on date "+date;
        System.out.println(log);
        db.addLog(log);

        if (db.areRatesOnDate(date)) {
            log ="Fx rates of this date already exist in DB";
            db.addLog(log);
            return log;
        } else {
            db.loadFxRates(date);
            if (db.areRatesOnDate(date)) {
                log="Fx rates successfully loaded from LB";
                db.addLog(log);
                return log;
            } else {
                log= "Unable to load Fx rates... check connection.";
                db.addLog(log);
                return log;
            }
        }

    }

    private String getTextFromFile(String path) {
        byte[] data;
        try {
            InputStream is = CurrCalc.class.getClassLoader().getResourceAsStream(path);
            data = is.readAllBytes();
            String result = new String(data);
            return result; //Files.readString(Paths.get(fullPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Loading error";
    }
}
