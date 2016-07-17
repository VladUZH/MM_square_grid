/**
 * Created by Vladimir Petrov on 28.06.2016.
 */
public class Runner {
    public int prevExtreme;
    public int prevDC;
    public int extreme;
    public double deltaUp;
    public double deltaDown;
    public double osL;
    public int type;
    public boolean initalized;
    public double reference;

    public Runner(double threshUp, double threshDown, ATick aTick, int type){
        prevExtreme = aTick.price;
        prevDC = aTick.price;
        extreme = aTick.price;
        reference = aTick.price;


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
            if( Math.log(aTick.price/(double) extreme) * 100 >= deltaUp ){
                prevExtreme = extreme;
                type = 1;
                extreme = aTick.price;
                prevDC = aTick.price;
                reference = aTick.price;
                return -1;
            }
            if( aTick.price < extreme ){
                extreme = aTick.price;
                osL = -(extreme - prevDC) / (double) prevDC * 100;
//                osL = -Math.log(extreme/(double) prevDC) * 100;

                if( Math.log(extreme/(double) reference) * 100 <= -deltaDown ){
                    reference = extreme;
                    return 2;
                }
                return 0;
            }
        }else if( type == 1 ){
            if( Math.log(aTick.price/(double) extreme) * 100 <= -deltaDown ){
                prevExtreme = extreme;
                type = -1;
                extreme = aTick.price;
                prevDC = aTick.price;
                reference = aTick.price;
                return 1;
            }
            if( aTick.price > extreme ){
                extreme = aTick.price;
                osL = (extreme - prevDC) / (double) prevDC * 100;
//                osL = Math.log(extreme/(double) prevDC) * 100;

                if( Math.log(extreme/(double) reference) * 100 >= deltaUp ){
                    reference = extreme;
                    return -2;
                }
                return 0;
            }
        }

        return 0;
    }


}