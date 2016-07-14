import javax.xml.ws.soap.Addressing;
import java.util.ArrayList;

/**
 * Created by Vladimir Petrov on 07.07.2016.
 */
public class MM_square_grid {

    public static final double LOWEST_DELTA = 0.1;
    public static final double DELTA_STEP = 0.1;
    public static final int N_DELTAS = 50;
    public static final int START_PRICE = 1000;
    public static final int N_GENERATIONS = 10000;
    public static final int MIN_PRICE_MOVE = 1;





    public static void main(String[] args){


        // for generated prices:
        ArrayList<String> namesGeneratedPrices = new ArrayList<>();
        ArrayList<int[]> generatedPricesList = new ArrayList<>();








        Trader[][] traders = new Trader[N_DELTAS][N_DELTAS];

        for (int stepX = 0; stepX < N_DELTAS; stepX++){
            for (int stepY = 0; stepY < N_DELTAS; stepY++){
                traders[stepX][stepY] = new Trader(LOWEST_DELTA + DELTA_STEP * stepY, LOWEST_DELTA + DELTA_STEP * stepX, 0.3, (int) Math.pow(-1, stepX * stepY +1));
            }
        }


        MM mm = new MM(MIN_PRICE_MOVE);

        int nIterations = 1000;

        int[] averagePrices = new int[N_GENERATIONS];

        for (int iteration = 0; iteration < nIterations; iteration++) {
            System.out.println("Iteration " + iteration + " started");

            int[] priceList = new int[N_GENERATIONS];


            ATick aTick = new ATick(START_PRICE);

            int listIndex = 0;
            for (int aGeneration = 0; aGeneration < N_GENERATIONS; aGeneration++) {
                int exceedVolume = 0;
                for (int stepX = 0; stepX < N_DELTAS; stepX++) {
                    for (int stepY = 0; stepY < N_DELTAS; stepY++) {
                        if (stepX > stepY){ // "<" - I region, ">" - III region, "==" - II region
//                        if (true) {
                            exceedVolume += (traders[stepX][stepY].runTrading(aTick));
                        }
                    }
                }
                int newPrice = mm.generateNextPrice(aTick.price, exceedVolume);
//                System.out.println(newPrice);
                priceList[listIndex] = newPrice;
                aTick = new ATick(newPrice);
                listIndex++;
            }


            if (iteration % 10 == 0){
                namesGeneratedPrices.add("Gen" + iteration);
                generatedPricesList.add(priceList);
            }


            for (listIndex = 0; listIndex < N_GENERATIONS; listIndex++) {
                averagePrices[listIndex] += priceList[listIndex];
            }


        }

        for (int listIndex = 0; listIndex < N_GENERATIONS; listIndex++){
            averagePrices[listIndex] /= nIterations;
        }

        namesGeneratedPrices.add("Average");
        generatedPricesList.add(averagePrices);
        AdditionalTools.saveResultsToFile("generatedPrices", namesGeneratedPrices, generatedPricesList);



    }














}
