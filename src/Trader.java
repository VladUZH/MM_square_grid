import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Vladimir Petrov on 28.05.2016.
 */
class Trader {
    Runner runner;
    boolean on;
    Random rand;
    double randLimit;
    int initialDCtype;
    int currentPosition; // LONG if +1, SHORT if -1
    int totalNumberOfPositions;
    int totalPnL;
    int tradedPrice;
    int nNothing = 0;
    int nEvents = 0;
    int dcPlusLong = 0;
    int thisPriceIE = 0;



    Trader(){};
    Trader(int deltaUp, int deltaDown, double randLimit, int initialDCtype){
        rand = new Random();
        on = false;
        this.initialDCtype = initialDCtype;
        runner = new Runner(deltaUp, deltaDown, initialDCtype);
        totalNumberOfPositions = 0;
        rand = new Random();
        this.randLimit = randLimit;
        totalPnL = 0;


    }


    public int runTrading(ATick aTick){ // returns the opened volume
        int event = runner.run(aTick);

        if (event != 0){ // in case of DC or deltaStar

            thisPriceIE = 1;

            if (on){

                if (currentPosition > 0){
                    dcPlusLong += 1;
                }


                if (rand.nextDouble() < randLimit){ // should stay
                    nEvents++;
                    return 0;
                } else { // flip position: close previous and open an opposite
                    totalPnL += (aTick.price - tradedPrice) * currentPosition;
                    currentPosition = currentPosition * (-1);
                    totalNumberOfPositions += 1;
                    tradedPrice = aTick.price;
                    nEvents++;
                    return 2 * currentPosition;

                }
            }

            else if (!on){ // open only in case of DC (not OS)
                on = true;
                currentPosition = (rand.nextDouble() > 0.5 ? 1 : -1);
                totalNumberOfPositions += 1;
                tradedPrice = aTick.price;
                nEvents++;
                return 2 * currentPosition;
            }


        } else {
            nNothing++;
            thisPriceIE = 0;
        }
        return 0;

    }



};