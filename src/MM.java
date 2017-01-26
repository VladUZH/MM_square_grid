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
        alpha = minPriceStep / Math.sqrt(2);
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