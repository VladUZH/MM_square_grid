import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Vladimir Petrov on 16.06.2016.
 */
public class MM {

    double alpha;
    Random random;
    double minPriceStep;
    double roundCoeff;


    MM(double minPriceStep){
        alpha = minPriceStep / Math.sqrt(2);
        random = new Random();
        this.minPriceStep = minPriceStep;
        roundCoeff = 1.0 / minPriceStep;
    }

    public double generateNextPrice(double previousPrice, int exceedVolume){

        if (exceedVolume == 0){
            return previousPrice + (random.nextInt(2) * 2 - 1) * minPriceStep;
        } else {
            return previousPrice + computePriceShift(exceedVolume);
        }

    }



    public double computePriceShift(int exceedVolume){
        double priceShift = alpha * Math.sqrt(Math.abs(exceedVolume));
        if (exceedVolume < 0){
            return -Math.round(priceShift * roundCoeff) / roundCoeff;
        }
        else if (exceedVolume > 0){
            return Math.round(priceShift * roundCoeff) / roundCoeff;
        }
        else {
            return 0;
        }
    }

}
