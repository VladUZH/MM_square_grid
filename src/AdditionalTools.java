
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by vladimir on 12.04.16.
 */
public class AdditionalTools {

    class DeltaXandTicksNumberResult{
        public float deltaXPercents;
        public int numberOfTicks;

        DeltaXandTicksNumberResult(float inputDeltaXPErcents, int inputNumberOfTicks){
            deltaXPercents = inputDeltaXPErcents;
            numberOfTicks = inputNumberOfTicks;
        }


    }

    // 2015.01.01 22:00:09.355,0.99405,0.99351,0.1,0.75

    static Date stringToDate(String inputStringDate, String dateFormat){

        DateFormat formatDate = new SimpleDateFormat(dateFormat, Locale.US);
        try {
            Date date = formatDate.parse(inputStringDate);

            return date;

        } catch (ParseException e){
            e.printStackTrace();
            return null;
        }


    }

//    static void saveResultsToFile(String fileName, ArrayList<Float> firstColumn, ArrayList<Float> secondColumn){
//        try {
//            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
//            int index = 0;
//            while (index < firstColumn.size()){
//                writer.println(firstColumn.get(index) + ";" + secondColumn.get(index));
//                index += 1;
//            }
//            writer.close();
//            System.out.println("The result is saved like " + fileName);
//
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//
//    }

    static void saveResultsToFile(String fileName, ArrayList<Float> firstColumn, float[] secondColumn){
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            int index = 0;
            while (index < firstColumn.size()){
                writer.println(firstColumn.get(index) + ";" + secondColumn[index]);
                index += 1;
            }
            writer.close();
            System.out.println("The result is saved like " + fileName);

        } catch (IOException e){
            e.printStackTrace();
        }

    }


//    static void saveResultsToFile(String fileName, ArrayList<String> columnNames, ArrayList<ArrayList> columns){
//        try {
//            String dateString = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
//
//            fileName = fileName + "_" + dateString + ".csv";
//
//            PrintWriter writer = new PrintWriter("Results/" + fileName, "UTF-8");
//
//            String colimnString = "";
//            for (String columnName : columnNames){
//                colimnString += columnName + ";";
//            }
//            writer.println(colimnString);
//
//            int index = 0;
//            while (index < columns.get(0).size()){
//                String string = "";
//                for (ArrayList list : columns){
//                    string += list.get(index) + ";";
//                }
//                writer.println(string);
//                index += 1;
//            }
//            writer.close();
//            System.out.println("The result is saved like " + fileName);
//
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//
//    }


    static void saveResultsToFile(String fileName, ArrayList<String> columnNames, ArrayList<float[]> columns, boolean Float){
        try {
            String dateString = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());

            fileName = fileName + "_" + dateString + ".csv";

            PrintWriter writer = new PrintWriter("Results/" + fileName, "UTF-8");

            String colimnString = "";
            for (String columnName : columnNames){
                colimnString += columnName + ";";
            }
            writer.println(colimnString);

            int index = 0;
            while (index < columns.get(0).length){
                String string = "";
                for (float[] array : columns){
                    string += array[index] + ";";
                }
                writer.println(string);
                index += 1;
            }
            writer.close();
            System.out.println("The result is saved like " + fileName);

        } catch (IOException e){
            e.printStackTrace();
        }

    }


    static void saveResultsToFile(String fileName, ArrayList<String> columnNames, ArrayList<int[]> columns){
        try {
            String dateString = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());

            fileName = fileName + "_" + dateString + ".csv";

            PrintWriter writer = new PrintWriter("Results/" + fileName, "UTF-8");

            String colimnString = "";
            for (String columnName : columnNames){
                colimnString += columnName + ";";
            }
            writer.println(colimnString);

            int index = 0;
            while (index < columns.get(0).length){
                String string = "";
                for (int[] array : columns){
                    string += array[index] + ";";
                }
                writer.println(string);
                index += 1;
            }
            writer.close();
            System.out.println("The result is saved like " + fileName);

        } catch (IOException e){
            e.printStackTrace();
        }

    }



    static int[] GenerateLogSpace(int min, int max, int logBins)
    {
        int[] logList = new int[logBins];
        double m = 1.0 / (logBins - 1);
        double quotient =  Math.pow(max / min, m);

        logList[0] = min;

        for (int i = 1; i < logBins; i++){
            logList[i] = (int) (logList[i - 1] * quotient);
        }

        return logList;

    }

    static int[] GenerateLinSpace(int min, int max, int nBins)
    {
        int[] linList = new int[nBins];

        int step = (max - min) / nBins;

        int nextPoint = min;

        for (int i = 0; i < nBins; i++){
            linList[i] = nextPoint;
            nextPoint += step;
        }

        return linList;
    }


//    static void createGaussianRandomWalk(String startDate, int x_0, int deltaTSec, float sigma, int average, int numberOfTicks, int spread){
//
//        ArrayList<ATick> generatedList = new ArrayList<ATick>();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.US);
//
//        try {
//            Date tickDate = sdf.parse(startDate);
//            ATick aTick;
//
//            Random r = new Random();
//
//            int priceBid = x_0;
//            int priceAsk;
//
//            int tickNumber = 0;
//
//            while (tickNumber < numberOfTicks){
//                tickDate = DateUtil.addSeconds(tickDate, deltaTSec);
//                priceBid = priceBid + (int) (r.nextGaussian() * (sigma) + average);
//                priceAsk = priceBid + spread;
//
//                aTick = new ATick(tickDate, priceBid, priceAsk);
//                generatedList.add(aTick);
//                tickNumber += 1;
//            }
//
//            try {
//                String outputFileName = (x_0 + "_" + deltaTSec + "_" + sigma + "_" + average + "_" + numberOfTicks + "_" + spread + ".csv");
//                FileWriter fw = new FileWriter("C:/Users/Vladimir Petrov/IdeaProjects/Lykkex2/Data/GRW_simulations/" + outputFileName);
//
//                fw.write("Time (UTC),Ask,Bid,AskVolume,BidVolume\n");
//                for (ATick tick : generatedList) {
//                    fw.write(sdf.format(tick.getDateTick()) + "," + tick.getAsk() + "," + tick.getBid() + ",0,0\n");
//                }
//
//                fw.close();
//            } catch (IOException ex){
//                ex.printStackTrace();
//            }
//
//
//        } catch (ParseException e){
//            e.printStackTrace();
//        }
//
//    }



    public static class DateUtil {
        static Date addDays(Date date, int days) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, days); //minus number would decrement the days
            return cal.getTime();
        }
        static Date addSeconds(Date date, int seconds) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.SECOND, seconds); //minus number would decrement the seconds
            return cal.getTime();
        }
    }

//    public static RawData reduceNumberOfTicks(RawData inputData, float percentTrashHold){
//
//        RawData resultTicks = new RawData();
//        resultTicks.setDataName(inputData.getDataName());
//
//        int index = 0;
//        float benchMark = inputData.getAllData().get(index).getBid();
//
//        while (index < inputData.getAllData().size()){
//
//            if (Math.abs(inputData.getAllData().get(index).getBid() - benchMark) * 100 / (float) benchMark > percentTrashHold){
//
//                ATick aTick = inputData.getAllData().get(index);
//                resultTicks.addTick(aTick);
//                benchMark = inputData.getAllData().get(index).getBid();
//            }
//
//            index += 1;
//        }
//
//        return resultTicks;
//    }



//    public static ATick toAveragePrice(ATick inputTick){
//
//        int mediumPrice = (inputTick.getAsk() + inputTick.getBid()) / 2;
//
//        ATick resultTick = new ATick(inputTick.getDateTick(), mediumPrice, mediumPrice);
//
//        return resultTick;
//    }

    public static float[] IntArrayToFloat(int[] intArray){
        float[] floatArray = new float[intArray.length];
        for (int index = 0; index < intArray.length; index++){
            floatArray[index] = (float) intArray[index];
        }

        return floatArray;
    }





    public static class RecordPricesAndStates{

        PrintWriter writer;
        ArrayList<Trader> listOfTraders;

        public RecordPricesAndStates(ArrayList<Trader> listOfTraders){

            this.listOfTraders = listOfTraders;

            String dateString = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
            String fileName = "priceAndStates_" + Math.round(listOfTraders.get(0).randLimit * 100.f) / 100.0f + "_" + dateString + ".csv";
            try{
                writer = new PrintWriter("Results/" + fileName, "UTF-8");
                String stringToWrite = "Price;";
                for (Trader aTrader : listOfTraders){
                    stringToWrite += "IE_" + aTrader.runner.deltaUp + "_" + aTrader.runner.deltaDown + ";P_" + aTrader.runner.deltaUp + "_" + aTrader.runner.deltaDown + ";";
                }
                writer.println(stringToWrite);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void updateState(int currentPrice){

            String stringToWrite = Integer.toString(currentPrice) + ";"; // will form a total string. Here just put current price.

            for (Trader aTrader : listOfTraders){
                stringToWrite += aTrader.runner.IElatestPrice + ";" + (aTrader.traded ? -aTrader.currentPosition : aTrader.currentPosition) + ";";
            }
            writer.println(stringToWrite);
        }

        public void finish(){
            writer.close();
        }


    }


}
