import javax.xml.ws.soap.Addressing;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by Vladimir Petrov on 07.07.2016.
 */
public class MM_square_grid {

    public static final int LOWEST_DELTA = 1;
    public static final int DELTA_STEP = 1;
    public static final int N_DELTAS = 50;

    public static final String FILE_PATH = "D:/Data/OANDA/Positions/";
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static int nDecimal = 4;




    public static void main(String[] args){

        String fileName = "OANDA_historical_position_ratios_data_EUR_USD.csv";
//        String trainFileName = "EURUSD_2015_daily.csv";
        String trainFileName = "EURUSD_UTC_Ticks_Bid_2014-01-01_2015-01-01.csv";
//        String fileName = "OANDA_historical_position_ratios_data_USD_CHF.csv";

        ATick aTick;

        Trader[][] traders1 = new Trader[N_DELTAS][N_DELTAS];

        Random rand = new Random(1);

        for (int stepX = 0; stepX < N_DELTAS; stepX++){
            for (int stepY = 0; stepY < N_DELTAS; stepY++){
                traders1[stepX][stepY] = new Trader(LOWEST_DELTA + DELTA_STEP * stepY, LOWEST_DELTA + DELTA_STEP * stepX, 0.3, (rand.nextDouble() > 0.5 ? 1 : -1));
            }
        }




        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_PATH + fileName));

            String thisLine = bufferedReader.readLine(); // header
            thisLine = bufferedReader.readLine(); // first string

            long firstRealDate = Long.valueOf(thisLine.split(",")[0]);


            // in this part we train agents by a real data one year before the OANDA data:

            try {
                BufferedReader trainBufferedReader = new BufferedReader(new FileReader(FILE_PATH + "../../" + trainFileName));

                String line = trainBufferedReader.readLine();
                String[] elements;

                boolean notEnd = true;

                while (notEnd){
                    elements = trainBufferedReader.readLine().split(",");


                    int intPrice = (int) (Double.parseDouble(elements[1]) * Math.pow(10, nDecimal));
                    aTick = new ATick(intPrice);
                    for (int stepX = 0; stepX < N_DELTAS; stepX++) {
                        for (int stepY = 0; stepY < N_DELTAS; stepY++) {
                        if (stepX == stepY){ // "<" - I region, ">" - III region, "==" - II region
//                            if (true) {
                                traders1[stepX][stepY].runTrading(aTick);
                            }
                        }
                    }


                    Date date = dateFormat.parse(elements[0]);

                    System.out.println(elements[0]);

                    if (date.after(new Date(firstRealDate * 1000L))){
                        notEnd = false;
                    }

                }

            } catch (Exception ex){
                ex.printStackTrace();
            }

            // in the following part we use the same data which OANDA show:








            String line;
            String[] components;



            String dateString = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
            String outFileName = "RESULT_" + dateString + "_" + fileName;
            PrintWriter writer = new PrintWriter("Results/" + outFileName, "UTF-8");
            writer.println("Timestamp;Price;OANDA_pctLong;Agents_pctLong");

            while ((line = bufferedReader.readLine()) != null) {
                components = line.split(",");


                int intPrice = (int) (Double.parseDouble(components[2]) * Math.pow(10, nDecimal));

                aTick = new ATick(intPrice);

                int nLong = 0;
                int nShort = 0;

                for (int stepX = 0; stepX < N_DELTAS; stepX++) {
                    for (int stepY = 0; stepY < N_DELTAS; stepY++) {
                        if (stepX == stepY){ // "<" - I region, ">" - III region, "==" - II region
//                        if (true) {
                            int traderPosition = traders1[stepX][stepY].runTrading(aTick);
                            if (traderPosition > 0){
                                nLong++;
                            } else if (traderPosition < 0){
                                nShort++;
                            }
                        }
                    }
                }

                float fractionLong = (nLong) * 1.0f / (nLong + nShort) * 100;

                System.out.println("OANDA: " + components[1] + ", Agents: " + fractionLong);


                writer.println(components[0] + ";" + components[2] + ";" + components[1] + ";" + fractionLong);






            }

            writer.close();


        } catch (IOException ex){
            ex.printStackTrace();
        }


    }





}
