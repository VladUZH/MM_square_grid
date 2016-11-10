import javax.xml.ws.soap.Addressing;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Vladimir Petrov on 07.07.2016.
 */
public class MM_square_grid {

    public static final int LOWEST_DELTA = 1;
    public static final int DELTA_STEP = 1;
    public static final int N_DELTAS = 50;

    public static final String FILE_PATH = "D:/Data/OANDA/Positions/";




    public static void main(String[] args){

//        String fileName = "OANDA_historical_position_ratios_data_EUR_USD.csv";
        String fileName = "OANDA_historical_position_ratios_data_USD_CHF.csv";

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

            String line;
            String[] components;

            boolean firstString = true;

            int nDecimal = 0;

            String outFileName = "RESULT_" + fileName;
            PrintWriter writer = new PrintWriter("Results/" + outFileName, "UTF-8");
            writer.println("Timestamp;Price;OANDA_pctLong;Agents_pctLong");

            while ((line = bufferedReader.readLine()) != null) {
                components = line.split(",");

                if (firstString){
                    nDecimal = 4;
                    firstString = false;
                }

                int intPrice = (int) (Double.parseDouble(components[2]) * Math.pow(10, nDecimal));

                aTick = new ATick(intPrice);

                int nLong = 0;
                int nShort = 0;

                for (int stepX = 0; stepX < N_DELTAS; stepX++) {
                    for (int stepY = 0; stepY < N_DELTAS; stepY++) {
//                        if (stepX == stepY){ // "<" - I region, ">" - III region, "==" - II region
                        if (true) {
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
