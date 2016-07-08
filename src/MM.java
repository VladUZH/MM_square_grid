import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Vladimir Petrov on 16.06.2016.
 */
public class MM {

    double alpha;
    Random random;

    MM(double alpha){
        this.alpha = alpha;
        random = new Random();
    }

    public double generateNextPrice(double previousPrice, int exceedVolume, double minPriceStep){

        if (exceedVolume == 0){
            return previousPrice + (random.nextInt(2) * 2 - 1) * minPriceStep;
        } else {
            return previousPrice + computePriceShift(exceedVolume);
        }

    }



    public double computePriceShift(int exceedVolume){
        double priceShift = alpha * Math.sqrt(Math.abs(exceedVolume));
        if (exceedVolume < 0){
            return -priceShift;
        }
        else if (exceedVolume > 0){
            return priceShift;
        }
        else {
            return 0;
        }
    }

}
