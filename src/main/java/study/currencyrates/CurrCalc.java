package study.currencyrates;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class CurrCalc {
    Database db = new Database("currdb");

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
            HttpServletRequest request) {


        String ip = request.getRemoteAddr();
        String log = ip + " requested to trade " + amount1 + " " + curr1 + " into " + curr2;
        System.out.println(log);
        db.addLog(log);
        double rate1 = db.getRate(curr1);
        if (rate1 == 0) {
            return "no such currency in database " + curr1;
        }
        double rate2 = db.getRate(curr2);
        if (rate1 == 0) {
            return "no such currency in database " + curr2;
        }
        double result = amount1 / rate1 * rate2;

        return amount1 + " " + curr1 + " exchanged to " + curr2 + " equals: " + result;
    }

    private static String getTextFromFile(String path) {
        try {
            URI fullPath = CurrCalc.class.getClassLoader().getResource(path).toURI();
            return Files.readString(Paths.get(fullPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Loading error";
    }
}
