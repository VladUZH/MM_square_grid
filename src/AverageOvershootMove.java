import java.util.ArrayList;

/**
 * Created by Vladimir Petrov on 28.04.2016.
 */
public class AverageOvershootMove {

    public int nSteps;
    public float[] arrayOfDeltas;
    public ArrayList<Runner> listOfDCOS;
    public double[] totalOSup;
    public double[] totalOSdown;
    public long[] nOSup;
    public long[] nOSdown;
    public float[] massOfAverageUp;
    public float[] massOfAverageDown;
    public float[] massOfAverageTotal;
    public String fileName;
    public boolean initialized;



    AverageOvershootMove(float minDelta, float maxDelta, int nSteps, boolean logScale, String fileNameInput){

        this.nSteps = nSteps;
        initialized = false;

        if (logScale){
            arrayOfDeltas = AdditionalTools.GenerateLogSpace(minDelta, maxDelta, nSteps);
        } else {
            arrayOfDeltas = AdditionalTools.GenerateLinSpace(minDelta, maxDelta, nSteps);
        }


        listOfDCOS = new ArrayList<Runner>();

        for (float delta : arrayOfDeltas){
            listOfDCOS.add(new Runner(delta, delta, -1));
        }

        totalOSdown = new double[nSteps];
        totalOSup = new double[nSteps];
        nOSup = new long[nSteps];
        nOSdown = new long[nSteps];
        massOfAverageUp = new float[nSteps];
        massOfAverageDown = new float[nSteps];
        massOfAverageTotal = new float[nSteps];

        fileName = fileNameInput;

    }




    public void run(ATick aTick){

        if (!initialized){
            initialized = true;
//            System.out.println("Average Overshoot Move initialized");
        }

        int indexOfelement = 0;
        for (Runner aDcOs : listOfDCOS){
            int status = aDcOs.run(aTick);
            if (status == 1) {
                totalOSdown[indexOfelement] += aDcOs.osL;
//                System.out.println(indexOfelement + " " + aDcOs.osL);
                nOSdown[indexOfelement] += 1;
            } else if (status == -1){
                totalOSup[indexOfelement] += aDcOs.osL;
//                System.out.println(indexOfelement + " " + aDcOs.osL);
                nOSup[indexOfelement] += 1;
            }
            indexOfelement += 1;

        }
    }



    public void finish(){

        int stepNumber = 0;
        while (stepNumber < nSteps) {
            massOfAverageUp[stepNumber] = (float) totalOSup[stepNumber] / nOSup[stepNumber];
            massOfAverageDown[stepNumber] = (float) totalOSdown[stepNumber] / nOSdown[stepNumber];
            massOfAverageTotal[stepNumber] = (float) (totalOSup[stepNumber] + totalOSdown[stepNumber]) / (nOSup[stepNumber] + nOSdown[stepNumber]);
            stepNumber += 1;
        }

//        int index = 0;
//        while (index < arrayOfUPPoints.size()){
//            System.out.println(arrayOfUPPoints.get(index) + "  " + arrayOfDOWNPoints.get(index) + " " + massOfAverageValues.get(index));
//            index += 1;
//        }
//
//        ArrayList<ArrayList> columns = new ArrayList<ArrayList>();
//        columns.add(arrayOfUPPoints);
//        columns.add(arrayOfDOWNPoints);
//        columns.add(massOfAverageValues);
//
//        ArrayList<String> columnNames = new ArrayList<String>();
//        columnNames.add("delta_UP");
//        columnNames.add("delta_DOWN");
//        columnNames.add("Av_OS_move");
//
//
//
//        AdditionalTools.saveResultsToFile("averageOvershootMove_" + fileName + "_downFraction_" + deltaDownFractionOfDeltaUP, columnNames, columns);

    }


}
