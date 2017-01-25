import javax.xml.ws.soap.Addressing;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Vladimir Petrov on 07.07.2016.
 */
public class MM_square_grid {

    public static final int LOWEST_DELTA = 1;
    public static final int DELTA_STEP = 1;
    public static final int N_DELTAS = 2;
    public static final int START_PRICE = 0;
    public static final int N_GENERATIONS = 10000; // how many generations in one experiment
    public static final int MIN_PRICE_MOVE = 1;
    public static final int OS_STEPS = 100;





    public static void main(String[] args){


        // for generated prices:
        ArrayList<String> namesGeneratedPrices = new ArrayList<>();
        ArrayList<int[]> generatedPricesList = new ArrayList<>();
        long[] sumPrices = new long[N_GENERATIONS];
        int[] averagePrices = new int[N_GENERATIONS];


        // for average OS length:
        ArrayList<String> namesAverageOvershoot = new ArrayList<>();
        ArrayList<float[]> averageOvershootList = new ArrayList<>();
        float[] OSup = new float[OS_STEPS];
        float[] OSdown = new float[OS_STEPS];
        float[] OStotal = new float[OS_STEPS];

        // for how many times agents trade:
        ArrayList<String> namesHowManyTrades = new ArrayList<>();
        ArrayList<int[]> averageHowManyList = new ArrayList<>();
        int[][] totalEveryTrade = new int[N_DELTAS][N_DELTAS];

        // for total PnL of each agent:
        ArrayList<String> namesTotalPnL = new ArrayList<>();
        ArrayList<int[]> averageTotalPnL = new ArrayList<>();
        int[][] totalTotalPnL = new int[N_DELTAS][N_DELTAS];

        // for net volume:
        ArrayList<String> namesNetVolume = new ArrayList<>();
        ArrayList<int[]> netVolumeList = new ArrayList<>();
        long[] sumNetVolume = new long[N_GENERATIONS];
        int[] averageNetVolume = new int[N_GENERATIONS];

        // for the price and state statistic:
        ArrayList<String> namesPriceAndState = new ArrayList<>();
        ArrayList<int[]> priceAndStateList = new ArrayList<>();




        int nIterations = 1; // how many experiments


        for (int iteration = 0; iteration < nIterations; iteration++) {


            AverageOvershootMove averageOvershootMove = new AverageOvershootMove(1, 101, OS_STEPS, false, "bla-bla");
            namesAverageOvershoot.add("Delta");
            averageOvershootList.add(AdditionalTools.IntArrayToFloat(averageOvershootMove.arrayOfDeltas));

            Trader[] traders1 = new Trader[N_DELTAS * N_DELTAS];

            Random rand = new Random(1);

            // chose this for the FULL grid
            for (int stepX = 0; stepX < N_DELTAS; stepX++){
                for (int stepY = 0; stepY < N_DELTAS; stepY++){         // for ALL regions
//                for (int stepY = stepX + 1; stepY < N_DELTAS; stepY++){ // for I region (\delta_{up} > \delta_{down})
//                int stepY = stepX;{                                     // for II region (\delta_{up} = \delta_{down})
//                for (int stepY = 0; stepY < stepX; stepY++){            // for III region (\delta_{up} < \delta_{down})
                        traders1[stepY * N_DELTAS + stepX] = new Trader(LOWEST_DELTA + DELTA_STEP * stepY, LOWEST_DELTA + DELTA_STEP * stepX, 0.3, (rand.nextDouble() > 0.5 ? 1 : -1));
                }
            }

            // chose this for the Ist region only
            for (int stepX = 0; stepX < N_DELTAS; stepX++){
                for (int stepY = stepX + 1; stepY < N_DELTAS; stepY++){
                    traders1[stepY * N_DELTAS + stepX] = new Trader(LOWEST_DELTA + DELTA_STEP * stepY, LOWEST_DELTA + DELTA_STEP * stepX, 0.3, (rand.nextDouble() > 0.5 ? 1 : -1));
                }
            }


            MM mm = new MM(MIN_PRICE_MOVE);

            int[] priceList = new int[N_GENERATIONS];
            int[] netVolume = new int[N_GENERATIONS];

            ATick aTick = new ATick(START_PRICE);

            int nOneUp = 0;
            int nFollowingOneUp = 0;
            int prevPriceMove = 0;

            int listIndex = 0;
            for (int aGeneration = 0; aGeneration < N_GENERATIONS; aGeneration++) {
                int exceedVolume = 0;

                for (Trader aTrader : traders1){
                    exceedVolume += aTrader.runTrading(aTick);
                }


                averageOvershootMove.run(aTick);
                int newPrice = mm.generateNextPrice(aTick.price, exceedVolume);

                if ((newPrice - aTick.price == 1) && (prevPriceMove == 1)){
                    nFollowingOneUp += 1;
                }

                if ((prevPriceMove = newPrice - aTick.price) == 1){
                    nOneUp += 1;
                }

//                System.out.println(newPrice);
                priceList[listIndex] = newPrice;
                sumPrices[listIndex] += newPrice;
                netVolume[listIndex] = exceedVolume;
                sumNetVolume[listIndex] += exceedVolume;
                aTick = new ATick(newPrice);
                listIndex++;
            }

            averageOvershootMove.finish();

            if (iteration % 10 == 0){
                System.out.println("Iteration " + iteration + " is executing");
                namesGeneratedPrices.add("Gen" + iteration);
                namesNetVolume.add("Gen" + iteration);
                generatedPricesList.add(priceList);
                netVolumeList.add(netVolume);

                namesAverageOvershoot.add("Gen" + iteration + "Up");
                averageOvershootList.add(averageOvershootMove.massOfAverageUp);
                namesAverageOvershoot.add("Gen" + iteration + "Down");
                averageOvershootList.add(averageOvershootMove.massOfAverageDown);
                namesAverageOvershoot.add("Gen" + iteration + "Total");
                averageOvershootList.add(averageOvershootMove.massOfAverageTotal);
            }

            for (listIndex = 0; listIndex < OS_STEPS; listIndex++){
                OSup[listIndex] += averageOvershootMove.massOfAverageUp[listIndex];
                OSdown[listIndex] += averageOvershootMove.massOfAverageDown[listIndex];
                OStotal[listIndex] += averageOvershootMove.massOfAverageTotal[listIndex];
            }

//            for (int stepX = 0; stepX < N_DELTAS; stepX++){
//                for (int stepY = 0; stepY < N_DELTAS; stepY++){
//                    totalEveryTrade[stepY][stepX] += traders1[stepX][stepY].totalNumberOfPositions; // should be like this to handle the final file structure problem.
//                    totalTotalPnL[stepY][stepX] += traders1[stepX][stepY].totalPnL; // should be like this to handle the final file structure problem.
//                }
//            }


        }



        for (int listIndex = 0; listIndex < N_GENERATIONS; listIndex++){
            averagePrices[listIndex] = (int) sumPrices[listIndex] / nIterations;
            averageNetVolume[listIndex] = (int) sumNetVolume[listIndex] / nIterations;

        }

        for (int listIndex = 0; listIndex < OS_STEPS; listIndex++){
            OSup[listIndex] /= nIterations;
            OSdown[listIndex] /= nIterations;
            OStotal[listIndex] /= nIterations;

        }

        for (int stepX = 0; stepX < N_DELTAS; stepX++){
            for (int stepY = 0; stepY < N_DELTAS; stepY++){
                totalEveryTrade[stepX][stepY] /= (float) nIterations;
            }
        }

        for (int stepX = 0; stepX < N_DELTAS; stepX++){
            for (int stepY = 0; stepY < N_DELTAS; stepY++){
                totalTotalPnL[stepX][stepY] /= (float) nIterations;
            }
        }





        namesGeneratedPrices.add("Average");
        generatedPricesList.add(averagePrices);
        AdditionalTools.saveResultsToFile("generatedPrices", namesGeneratedPrices, generatedPricesList);

        namesNetVolume.add("Average");
        netVolumeList.add(averageNetVolume);
        AdditionalTools.saveResultsToFile("netVolume", namesNetVolume, netVolumeList);



        namesAverageOvershoot.add("AverageUP");
        averageOvershootList.add(OSup);
        namesAverageOvershoot.add("AverageDOWN");
        averageOvershootList.add(OSdown);
        namesAverageOvershoot.add("AverageTOTAL");
        averageOvershootList.add(OStotal);
        AdditionalTools.saveResultsToFile("averageOvershoots", namesAverageOvershoot, averageOvershootList, true);



//        for (int i = 0; i < N_DELTAS; i++){
//            namesHowManyTrades.add(Integer.toString(i));
//            averageHowManyList.add(totalEveryTrade[i]); // do not forget to turn 90° counterclockwise!
//        }
//        AdditionalTools.saveResultsToFile("averageHowManyTrades", namesHowManyTrades, averageHowManyList);
//
//        for (int i = 0; i < N_DELTAS; i++){
//            namesTotalPnL.add(Integer.toString(i));
//            averageTotalPnL.add(totalTotalPnL[i]); // do not forget to turn 90° counterclockwise!
//        }
//        AdditionalTools.saveResultsToFile("averageTotalPnL", namesTotalPnL, averageTotalPnL);



    }














}
