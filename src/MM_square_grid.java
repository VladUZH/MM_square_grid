/**
 * Created by Vladimir Petrov on 07.07.2016.
 */
public class MM_square_grid {

    public static final double LOWEST_DELTA = 0.1;
    public static final double DELTA_STEP = 0.05;
    public static final int N_DELTAS = 50;
    public static final double START_PRICE = 100.0;
    public static final double ALPHA = 2 * LOWEST_DELTA / 100.0 * START_PRICE;
    public static final int N_GENERATIONS = 10000;




    public static void main(String[] args){

        Trader[][] traders = new Trader[N_DELTAS][N_DELTAS];

        for (int stepX = 0; stepX < N_DELTAS; stepX++){
            for (int stepY = 0; stepY < N_DELTAS; stepY++){
                traders[stepX][stepY] = new Trader(LOWEST_DELTA + DELTA_STEP * stepX, LOWEST_DELTA + DELTA_STEP * stepY, 0.3, (int) Math.pow(-1, stepX * stepY +1));
            }
        }


        MM mm = new MM(ALPHA);

        int nIterations = 50;

        for (int lastY = 0; lastY < N_GENERATIONS - 1; lastY++) {

            double[] averagePrices = new double[N_GENERATIONS];


            for (int iteration = 0; iteration < nIterations; iteration++) {
                System.out.println("Iteration " + iteration + " started");

                double[] priceList = new double[N_GENERATIONS];


                ATick aTick = new ATick(START_PRICE);

                int listIndex = 0;
                for (int aGeneration = 0; aGeneration < N_GENERATIONS; aGeneration++) {
                    int exceedVolume = 0;
                    for (int stepX = 0; stepX < N_DELTAS; stepX++) {
                        for (int stepY = 0; stepY < N_DELTAS; stepY++) {
                            if (stepY * (N_DELTAS - 1) <= stepX * lastY) {
                                exceedVolume += (traders[stepX][stepY].runTrading(aTick));
                            }
                        }
                    }
                    double newPrice = mm.generateNextPrice(aTick.price, exceedVolume, ALPHA);
//                System.out.println(newPrice);
                    priceList[listIndex] = newPrice;
                    aTick = new ATick(newPrice);
                    listIndex++;
                }

                for (listIndex = 0; listIndex < N_GENERATIONS; listIndex++) {
                    averagePrices[listIndex] += priceList[listIndex];
                }


            }

            for (int listIndex = 0; listIndex < N_GENERATIONS; listIndex++){
                System.out.println(averagePrices[listIndex] / nIterations);
            }

        }




    }









}
