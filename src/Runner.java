/**
 * Created by Vladimir Petrov on 28.06.2016.
 */
public class Runner {
    public double prevExtreme;
    public double prevDC;
    public double extreme;
    public double deltaUp;
    public double deltaDown;
    public double osL;
    public int type;
    public boolean initalized;
    public double reference;
    public double totalOSup;
    public int nDCup;
    public double totalOSdown;
    public int nDCdown;

    public Runner(double threshUp, double threshDown, ATick aTick, int type){
        prevExtreme = aTick.price;
        prevDC = aTick.price;
        extreme = aTick.price;
        reference = aTick.price;
        totalOSdown = totalOSup = 0;
        if (type == -1){
            nDCdown = 1;
            nDCup = 0;
        } else if (type == 1){
            nDCdown = 0;
            nDCup = 1;
        }

        this.type = type; deltaUp = threshUp; deltaDown = threshDown; osL = 0.0; initalized = true;
    }


    public Runner(double threshUp, double threshDown, int type){
        deltaUp = threshUp; deltaDown = threshDown;
        initalized = false;
        this.type = type;
    }

    public int run(ATick aTick){
        if( aTick == null )
            return 0;

        if( !initalized ){
            osL = 0.0; initalized = true;
            prevExtreme = aTick.price;
            prevDC = aTick.price;
            extreme = aTick.price;
            reference = aTick.price;

            return 0;
        }

        if( type == -1 ){
            if( Math.log(aTick.price/extreme) * 100 >= deltaUp ){
                prevExtreme = extreme;
                type = 1;
                extreme = aTick.price;
                prevDC = aTick.price;
                reference = aTick.price;
                totalOSdown += osL;
                nDCup += 1;
                return -1;
            }
            if( aTick.price < extreme ){
                extreme = aTick.price;
                osL = -Math.log(extreme/prevDC) * 100;

                if( Math.log(extreme/reference) * 100 <= -deltaDown ){
                    reference = extreme;
                    return 2;
                }
                return 0;
            }
        }else if( type == 1 ){
            if( Math.log(aTick.price/extreme) * 100 <= -deltaDown ){
                prevExtreme = extreme;
                type = -1;
                extreme = aTick.price;
                prevDC = aTick.price;
                reference = aTick.price;
                totalOSup += osL;
                nDCdown += 1;
                return 1;
            }
            if( aTick.price > extreme ){
                extreme = aTick.price;
                osL = Math.log(extreme/prevDC) * 100;

                if( Math.log(extreme/reference) * 100 >= deltaUp ){
                    reference = extreme;
                    return -2;
                }
                return 0;
            }
        }

        return 0;
    }


}