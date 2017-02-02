import javax.xml.ws.soap.Addressing;
import java.io.*;
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

    public static final String FILE_PATH = "D:/Data/";

    public static int nDecimal = 4;

    public static int saveEvery = 10000;




    public static void main(String[] args){

        String[] names = new String[2];
        names[0] = "AUDJPY_UTC_Ticks_Bid_2015.02.02_2016.02.02.csv";
        names[1] = "AUDJPY_UTC_Ticks_Bid_2016.02.02_2017.01.31.csv";

        ATick aTick;

        Trader[][] traders1 = new Trader[N_DELTAS][N_DELTAS];

        Random rand = new Random(0);

        for (int stepX = 0; stepX < N_DELTAS; stepX++){
            for (int stepY = 0; stepY < N_DELTAS; stepY++){
                traders1[stepX][stepY] = new Trader(LOWEST_DELTA + DELTA_STEP * stepY, LOWEST_DELTA + DELTA_STEP * stepX, 0.3, (rand.nextDouble() > 0.5 ? 1 : -1));
            }
        }



        try {

            String line;
            String[] components;

            String dateString = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
            String outFileName = "RESULT_" + dateString + "_" + names[1];
            PrintWriter writer = new PrintWriter("Results/" + outFileName, "UTF-8");
            writer.println("Timestamp;Price;Agents_pctLong");

            int prevPrice = 0;
            int counter = 0;
            for (String fileName : names){

                BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_PATH + fileName));
                bufferedReader.readLine(); // to remove the header



                while ((line = bufferedReader.readLine()) != null) {
                    components = line.split(",");

                    int intPrice = (int) (Double.parseDouble(components[2]) * Math.pow(10, nDecimal));

                    if (intPrice == prevPrice){
                        continue;
                    }

                    aTick = new ATick(intPrice);

                    int nLong = 0;
                    int nShort = 0;

                    for (int stepX = 0; stepX < N_DELTAS; stepX++) {
                        for (int stepY = 0; stepY < N_DELTAS; stepY++) {
//                        if (stepX == stepY){ // "<" - I region, ">" - III region, "==" - II region
                            if (true) {
                                int traderPosition = traders1[stepX][stepY].runTrading(aTick);
                                if (traderPosition > 0){
                                    nLong += 2;
                                } else if (traderPosition < 0){
                                    nShort += 2;
                                }
                            }
                        }
                    }

                    float fractionLong = (float) nLong / (nLong + nShort) * 100;

//                    if (fractionLong == 100){
//                        System.out.println("100% long");
//                    }

                    if (counter % 10000 == 0){
                        System.out.println("Index = " + counter + ", Agents: " + fractionLong);
                    }

                    if (counter % saveEvery == 0){
                        writer.println(components[0] + ";" + components[2] + ";" + fractionLong);
                    }

                    prevPrice = intPrice;
                    counter++;
                }

            }

            writer.close();
            System.out.println("Results saved into the file    " + outFileName);

        } catch (IOException ex){
            ex.printStackTrace();
        }

    }



}
