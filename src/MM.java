import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Vladimir Petrov on 16.06.2016.
 */
public class MM {

    double alpha;
    Random random;
    int minPriceStep;


    MM(int minPriceStep){
        alpha = minPriceStep / Math.sqrt(2.0); // for sqrt
//        alpha = minPriceStep / 2.0; // for linear
//        alpha = minPriceStep / Math.log(2.0); // for log
//        alpha = Math.exp(1)/(Math.exp(1) - 1); // for (1-exp(-x))

        random = new Random();
        this.minPriceStep = minPriceStep;
    }

    public int generateNextPrice(int previousPrice, int exceedVolume){

        if (exceedVolume == 0){
            return previousPrice + (random.nextInt(2) * 2 - 1) * minPriceStep;
        } else {
            return previousPrice + computePriceShift(exceedVolume);
        }

    }



    public int computePriceShift(int exceedVolume){
        double priceShift = alpha * Math.sqrt(Math.abs(exceedVolume)); // sqrt function
//        double priceShift = alpha * (Math.abs(exceedVolume)); // linear function
//        double priceShift = alpha * Math.log(Math.abs(exceedVolume)); // log function
//        double priceShift = alpha * (1 - Math.exp(-Math.abs(exceedVolume))); // exp function


        if (exceedVolume < 0){
            return -(int) Math.floor(priceShift);
        }
        else if (exceedVolume > 0){
            return (int) Math.floor(priceShift);
        }
        else {
            return 0;
        }
    }

}