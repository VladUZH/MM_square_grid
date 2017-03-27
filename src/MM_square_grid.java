import javax.xml.ws.soap.Addressing;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Vladimir Petrov on 07.07.2016.
 */
public class MM_square_grid {

    public static final int LOWEST_DELTA = 1;
    public static final int DELTA_STEP = 1;
    public static final int N_DELTAS = 2;
    public static final int START_PRICE = 0;
    public static final int N_GENERATIONS = 100000; // how many generations in one experiment
    public static final int N_EXPERIMENTS = 1; // number of experiments
    public static final int MIN_PRICE_MOVE = 1;
    public static final int OS_STEPS = 10;





    public static void main(String[] args){

        for (float probFlip = 0.0f; probFlip <= 1.0f; probFlip += 0.02f){

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

//        // for the price and state statistic:
//        ArrayList<String> namesPriceAndState = new ArrayList<>();
//        ArrayList<int[]> priceAndStateList = new ArrayList<>();






            for (int iteration = 0; iteration < N_EXPERIMENTS; iteration++) {

//            namesPriceAndState.add("Price");
//            priceAndStateList.add(new int[N_GENERATIONS]);

                AverageOvershootMove averageOvershootMove = new AverageOvershootMove(1, 101, OS_STEPS, false, "bla-bla");
                namesAverageOvershoot.add("Delta");
                averageOvershootList.add(AdditionalTools.IntArrayToFloat(averageOvershootMove.arrayOfDeltas));

                ArrayList<Trader> traders1 = new ArrayList<>();

                Random rand = new Random(1);

                for (int stepX = 0; stepX < N_DELTAS - 1; stepX++){
                    for (int stepY = 0; stepY < N_DELTAS; stepY++){         // for ALL regions
//                for (int stepY = stepX + 1; stepY < N_DELTAS; stepY++){ // for I region (\delta_{up} > \delta_{down})
//                int stepY = stepX;{                                     // for II region (\delta_{up} = \delta_{down})
//                for (int stepY = 0; stepY < stepX; stepY++){            // for III region (\delta_{up} < \delta_{down})
                        int deltaUp = LOWEST_DELTA + DELTA_STEP * stepY;
                        int deltaDown = LOWEST_DELTA + DELTA_STEP * stepX;
                        traders1.add(new Trader(deltaUp, deltaDown, probFlip, (rand.nextDouble() > 0.5 ? 1 : -1)));
//                    namesPriceAndState.add(String.format("A_%s_%s", deltaUp, deltaDown));
//                    priceAndStateList.add(new int[N_GENERATIONS]);
                    }
                }



                MM mm = new MM(MIN_PRICE_MOVE);

                int[] priceList = new int[N_GENERATIONS];
                int[] netVolume = new int[N_GENERATIONS];

                ATick aTick = new ATick(START_PRICE);

                int nOneUp = 0;
                int nFollowingOneUp = 0;
                int prevPriceMove = 0;

                AdditionalTools.RecordPricesAndStates recordPricesAndStates = new AdditionalTools.RecordPricesAndStates(traders1);

                int listIndex = 0;
                for (int aGeneration = 0; aGeneration < N_GENERATIONS; aGeneration++) {

//                priceAndStateList.get(0)[aGeneration] = aTick.price; // put price to this list of statistics

                    int exceedVolume = 0;

                    for (int n = 0; n < traders1.size(); n++){
                        exceedVolume += traders1.get(n).runTrading(aTick);
//                    priceAndStateList.get(n + 1)[aGeneration] = traders1.get(n).thisPriceIE;
                    }

                    recordPricesAndStates.updateState(aTick.price);



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

                recordPricesAndStates.finish();

                averageOvershootMove.finish();

//            AdditionalTools.saveResultsToFile("priceAndState", namesPriceAndState, priceAndStateList);

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
                averagePrices[listIndex] = (int) sumPrices[listIndex] / N_EXPERIMENTS;
                averageNetVolume[listIndex] = (int) sumNetVolume[listIndex] / N_EXPERIMENTS;

            }

            for (int listIndex = 0; listIndex < OS_STEPS; listIndex++){
                OSup[listIndex] /= N_EXPERIMENTS;
                OSdown[listIndex] /= N_EXPERIMENTS;
                OStotal[listIndex] /= N_EXPERIMENTS;

            }

            for (int stepX = 0; stepX < N_DELTAS; stepX++){
                for (int stepY = 0; stepY < N_DELTAS; stepY++){
                    totalEveryTrade[stepX][stepY] /= (float) N_EXPERIMENTS;
                }
            }

            for (int stepX = 0; stepX < N_DELTAS; stepX++){
                for (int stepY = 0; stepY < N_DELTAS; stepY++){
                    totalTotalPnL[stepX][stepY] /= (float) N_EXPERIMENTS;
                }
            }





            namesGeneratedPrices.add("Average");
            generatedPricesList.add(averagePrices);
//        AdditionalTools.saveResultsToFile("generatedPrices", namesGeneratedPrices, generatedPricesList);

            namesNetVolume.add("Average");
            netVolumeList.add(averageNetVolume);
//        AdditionalTools.saveResultsToFile("netVolume", namesNetVolume, netVolumeList);



            namesAverageOvershoot.add("AverageUP");
            averageOvershootList.add(OSup);
            namesAverageOvershoot.add("AverageDOWN");
            averageOvershootList.add(OSdown);
            namesAverageOvershoot.add("AverageTOTAL");
            averageOvershootList.add(OStotal);
//        AdditionalTools.saveResultsToFile("averageOvershoots", namesAverageOvershoot, averageOvershootList, true);



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

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception ex){
                ex.printStackTrace();
            }

        }





    }














}
