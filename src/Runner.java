/**
 * Created by Vladimir Petrov on 28.06.2016.
 */
public class Runner {
    public int prevExtreme;
    public int prevDC;
    public int extreme;
    public int deltaUp;
    public int deltaDown;
    public int osL;
    public int type; // if 1 - wait for DC_UP, if -1 - wait for DC_Down
    public boolean initalized;
    public int reference;

    public Runner(int threshUp, int threshDown, ATick aTick, int type){
        prevExtreme = aTick.price;
        prevDC = aTick.price;
        extreme = aTick.price;
        reference = aTick.price;


        this.type = type; deltaUp = threshUp; deltaDown = threshDown; osL = 0; initalized = true;
    }


    public Runner(int threshUp, int threshDown, int type){
        deltaUp = threshUp; deltaDown = threshDown;
        initalized = false;
        this.type = type;
    }

    public int run(ATick aTick){
        if( aTick == null )
            return 0;

        if( !initalized ){
            osL = 0; initalized = true;
            prevExtreme = aTick.price;
            prevDC = aTick.price;
            extreme = aTick.price;
            reference = aTick.price;

            return 0;
        }

        if( type == -1 ){
            if( aTick.price - extreme >= deltaUp ){
                prevExtreme = extreme;
                type = 1;
                extreme = aTick.price;
                prevDC = aTick.price;
                reference = aTick.price;
                return 1;
            }
            if( aTick.price < extreme ){
                extreme = aTick.price;
                osL = -(extreme - prevDC);
//                osL = -Math.log(extreme/(double) prevDC) * 100;

                if( (extreme - reference) <= -deltaDown ){
                    reference = extreme;
                    return -2;
                }
                return 0;
            }
        }else if( type == 1 ){
            if( (aTick.price - extreme) <= -deltaDown ){
                prevExtreme = extreme;
                type = -1;
                extreme = aTick.price;
                prevDC = aTick.price;
                reference = aTick.price;
                return -1;
            }
            if( aTick.price > extreme ){
                extreme = aTick.price;
                osL = extreme - prevDC;
//                osL = Math.log(extreme/(double) prevDC) * 100;

                if( (extreme - reference) >= deltaUp ){
                    reference = extreme;
                    return 2;
                }
                return 0;
            }
        }

        return 0;
    }


}