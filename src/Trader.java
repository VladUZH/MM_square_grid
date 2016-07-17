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
    int currentPosition;
    int totalNumberOfPositions;



    Trader(){};
    Trader(int deltaUp, int deltaDown, double randLimit, int initialDCtype){
        rand = new Random();
        on = false;
        this.initialDCtype = initialDCtype;
        runner = new Runner(deltaUp, deltaDown, initialDCtype);
        totalNumberOfPositions = 0;
        rand = new Random();
        this.randLimit = randLimit;

    }


    public int runTrading(ATick aTick){ // returns the opened volume
        int event = runner.run(aTick);

        if (event != 0){ // in case of overshoot or deltaStar

            if (on){
                if (rand.nextDouble() < randLimit){ // should stay
                    return 0;
                } else { // flip position: close previous and open an opposite one
                    currentPosition = currentPosition * (-1);
                    totalNumberOfPositions += 1;
                    return (2 * currentPosition);

                }
            }

            else if (!on){
                on = true;
                currentPosition = initialDCtype;
                totalNumberOfPositions += 1;
                return (2 * currentPosition);
            }


        }
        return 0;

    }



};