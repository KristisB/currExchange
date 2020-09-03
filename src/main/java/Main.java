import spark.Spark;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Database db = new Database("currdb");
        Spark.port(80);
        Spark.get("/home", (request, response) -> getTextFromFile("home.html"));

        Spark.post("/exchange", (request, response) -> {
            String curr1 = request.queryParams("curr1");
            String curr2 = request.queryParams("curr2");
            double amount1 = Double.parseDouble(request.queryParams("amount"));
            String ip= request.ip();
            String log= ip+" requested to trade "+amount1+" " +curr1+" into "+ curr2;
            System.out.println(log);
            db.addLog(log);
            double rate1 = db.getRate(curr1);
            if (rate1 ==0){
                return "no such currency in database "+ curr1;
            }
            double rate2= db.getRate(curr2);
            if (rate1 ==0){
                return "no such currency in database "+ curr2;
            }
            double result=amount1/rate1*rate2;

            return amount1+" "+curr1+" exchanged to "+curr2 + " equals: " + result;
        });
    }


    private static String getTextFromFile(String path) {
        try {
            URI fullPath = Main.class.getClassLoader().getResource(path).toURI();
            return Files.readString(Paths.get(fullPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Loading error";
    }
}
