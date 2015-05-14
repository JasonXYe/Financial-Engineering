import com.google.common.collect.Lists;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Created with IntelliJ IDEA.
 * User: Xiao Ye
 * Date: 4/23/15
 * Time: 2:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class VPIN {
    public static void main(String[] args) throws ParseException {
        //String directory = "c:\\algo";
        String directory = args[0].toString();
        String filePath = args[1].toString();
        String ticker = args[2].toString();
        String level = args[3].toString();
        int buckets = Integer.valueOf(args[4].toString());
        int vpinBarSize = Integer.valueOf(args[5].toString());
        double volumeBarSize;

        // find the first time bar and construct a time bars linked list
        TimeBar firstBar = readFile(filePath, level);

        // calculate volume bar size
        volumeBarSize = calculateAverageVolume(firstBar, buckets);

        // bucket time bars into volume bars of equal sizes
        List<VolumeBar> volumeBars = buildVolumeBars(volumeBarSize, firstBar);
        //printVolumeBars(volumeBars);
        List<VPINBar> vpinBars = Lists.newArrayList();

        // further process updated time bars with new bars added to calculate price difference
        calculatePriceDifferenceAmongTimeBars(firstBar);
        smoothDateRollEffects(firstBar);

        // calculate sigma(delta price) for each volume bar
        for(VolumeBar volumeBar : volumeBars) {
            calculateVolumeBarStd(volumeBar);
            attachStartAndEndPointsVolumeBar(volumeBar);
        }

        // calculate buy and sell volume for time bars within each volume bar
        for(VolumeBar volumeBar : volumeBars) {
            calculateBuySellVolumeAndOrderImbalance(volumeBar);
            System.out.println(String.format("Time: %s, Volume: %f, OI: %f", volumeBar.getEndTime().toString(), volumeBarSize, volumeBar.getOrderImbalance()));
        }

        // calculate vpin bars
        calculateVPINBars(vpinBars, volumeBars, vpinBarSize, volumeBarSize);

        //Output results in console for eyeballing obvious mistake
        System.out.println("Results");
        for(VPINBar vpinBar : vpinBars) {
            System.out.println(String.format("%s,%.5f,%.2f",printDateTime(vpinBar.getEndTime()), vpinBar.getVpin(),
                    vpinBar.getLastTimeBarPrice()));
        }

        //also write the result to a csv file
        generateCsvFile(vpinBars, directory, ticker, buckets, vpinBarSize);

    }

    public static void smoothDateRollEffects(TimeBar firstTimeBar) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        TimeBar currentTimeBar = firstTimeBar.getNextBar();
        TimeBar previousTimeBar = null;
        while(currentTimeBar!=null) {
            previousTimeBar = currentTimeBar.getPreviousBar();
            if(!fmt.format(previousTimeBar.getTradeTime()).equals(fmt.format(currentTimeBar.getTradeTime()))) {
                currentTimeBar.setDeltaPrice(0.0);
            }
            currentTimeBar = currentTimeBar.getNextBar();
        }
    }

    public static void calculateVPINBars(List<VPINBar> vpinBars, List<VolumeBar> volumeBars, int size, double volMean) {
        for(int k=0; k<volumeBars.size()-size+1; k++) {
            double OI = 0.0;
            for(int n=k; n<k+size; n++) {
                OI = OI+volumeBars.get(n).getOrderImbalance();
            }
            double vpin = OI/(volMean*size);
            Date start = volumeBars.get(k).getStartTime();
            Date end = volumeBars.get(k+size-1).getEndTime();
            int lastPriceIndex = volumeBars.get(k+size-1).getTimeBars().size()-1;
            double lastPrice = volumeBars.get(k+size-1).getTimeBars().get(lastPriceIndex).getPrice();
            Date mid = new Date((start.getTime()+end.getTime())/2);
            VPINBar vpinBar = new VPINBar(start, end, mid, vpin);
            vpinBar.setLastTimeBarPrice(lastPrice);
            vpinBars.add(vpinBar);
        }

    }

    public static void calculateBuySellVolumeAndOrderImbalance(VolumeBar volumeBar) {
        double sigma = volumeBar.getStd();
        NormalDistribution normal = new NormalDistribution(0,1);
        for(TimeBar timeBar : volumeBar.getTimeBars()) {
            double buyProbability = 0.0;
            if(sigma!=0.0) {
                buyProbability = normal.cumulativeProbability((timeBar.getDeltaPrice())/sigma);
            } else {
                if(timeBar.getDeltaPrice()>0.0)
                    buyProbability = 1.0;
                if(timeBar.getDeltaPrice()==0.0)
                    buyProbability = 0.5;
                if(timeBar.getDeltaPrice()<0.0)
                    buyProbability = 0.0;
            }

            double sellProbability = 1 - buyProbability;
            timeBar.setBuy(timeBar.getVolume()*buyProbability);
            timeBar.setSell(timeBar.getVolume()*sellProbability);
            timeBar.setOI(Math.abs(timeBar.getBuy()-timeBar.getSell()));
        }

        double volumeBarTotalBuy = 0.0;
        double volumeBarTotalSell = 0.0;

        for(TimeBar timeBar : volumeBar.getTimeBars()) {
            volumeBarTotalBuy = volumeBarTotalBuy + timeBar.getBuy();
            volumeBarTotalSell = volumeBarTotalSell + timeBar.getSell();
        }

        volumeBar.setOrderImbalance(Math.abs(volumeBarTotalBuy-volumeBarTotalSell));
    }

    public static void attachStartAndEndPointsVolumeBar(VolumeBar volBar) {
        int lastTimeBarIndex = volBar.getTimeBars().size()-1;
        volBar.setStartTime(volBar.getTimeBars().get(0).getTradeTime());
        volBar.setEndTime(volBar.getTimeBars().get(lastTimeBarIndex).getTradeTime());
    }

    public static void calculateVolumeBarStd(VolumeBar volBar) {
        double r2sum = 0.0;
        double sum = 0.0;
        List<TimeBar> timeBars = volBar.getTimeBars();
        for(int i=0; i<timeBars.size(); i++) {
            sum = sum + timeBars.get(i).getDeltaPrice();
        }
        double mean = sum/timeBars.size();

        for(int j=0; j<timeBars.size(); j++) {
            r2sum = r2sum + Math.pow((timeBars.get(j).getDeltaPrice()-mean),2);
        }

        double sigma = 0.0;
        if(timeBars.size()>1) {
            sigma = Math.sqrt(r2sum/(timeBars.size()-1));
        }
        volBar.setStd(sigma);
    }

    public static void calculatePriceDifferenceAmongTimeBars(TimeBar firstTimeBar) {
        TimeBar currentTimeBar = firstTimeBar.getNextBar();

        while(currentTimeBar!=null) {
            currentTimeBar.setDeltaPrice(currentTimeBar.getPrice()-currentTimeBar.getPreviousBar().getPrice());
            currentTimeBar = currentTimeBar.getNextBar();
        }

    }

    public static List<VolumeBar> buildVolumeBars(double volumeBarSize, TimeBar firstBar) {
        List<VolumeBar> volumeBars = Lists.newArrayList();
        TimeBar currentTimeBar = firstBar;
        while(currentTimeBar!=null) {
            double volumeFilled = 0.0;
            VolumeBar volBar = new VolumeBar();
            while(volumeFilled < volumeBarSize && currentTimeBar!=null) {
                if(currentTimeBar.getVolume() <= (volumeBarSize-volumeFilled)) {
                    volBar.getTimeBars().add(currentTimeBar);
                    volumeFilled = volumeFilled + currentTimeBar.getVolume();
                    currentTimeBar = currentTimeBar.getNextBar();
                }
                // if the next time bar is too large to completely fit in the current volume bar, we split this time bar into two time bars
                // when splitting, we use linear impact model
                else {
                    TimeBar firstHalf = new TimeBar();
                    firstHalf.setPreviousBar(currentTimeBar.getPreviousBar());

                    firstHalf.setNextBar(currentTimeBar);
                    firstHalf.setTradeTime(currentTimeBar.getTradeTime());
                    if(currentTimeBar.getPreviousBar()!=null){
                        double firstHalfPriceLinearEstimation = currentTimeBar.getPreviousBar().getPrice()+
                                (currentTimeBar.getPrice()-currentTimeBar.getPreviousBar().getPrice())*((volumeBarSize-volumeFilled)/currentTimeBar.getVolume());
                        firstHalf.setPrice(firstHalfPriceLinearEstimation);
                        // update the second half
                        currentTimeBar.getPreviousBar().setNextBar(firstHalf);
                        currentTimeBar.setPreviousBar(firstHalf);
                    } else {
                        firstHalf.setPrice(currentTimeBar.getPrice());
                    }
                    firstHalf.setVolume(volumeBarSize-volumeFilled);
                    volumeFilled = volumeFilled + firstHalf.getVolume();
                    currentTimeBar.setVolume(currentTimeBar.getVolume() - firstHalf.getVolume());
                    volBar.getTimeBars().add(firstHalf);
                }

            }
            volumeBars.add(volBar);
        }
        return volumeBars;
    }

    public static double calculateAverageVolume(TimeBar firstTimeBar, int buckets) {
        TimeBar currentNode = firstTimeBar;
        double totalVolume = 0;
        while(currentNode!=null) {
            totalVolume = totalVolume + currentNode.getVolume();
            currentNode = currentNode.getNextBar();
        }
        return totalVolume/buckets;
    }

    public static TimeBar readFile(String location, String level) throws ParseException {
        String csvFile = location;
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        DateFormat format;
        int init = 0;
        TimeBar firstBar = null;
        TimeBar currentBar = null;
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                String[] minuteData = line.split(cvsSplitBy);
                String data = minuteData[0];
                if("minute".equals(level))
                    format = new SimpleDateFormat("MMddyyyy hh:mm", Locale.ENGLISH);
                else
                    format = new SimpleDateFormat("MMddyyyy hh:mm:ss", Locale.ENGLISH);
                Date date = format.parse(data);
                double price = Double.valueOf(minuteData[1]);
                int volume = Integer.valueOf(minuteData[2]);
                TimeBar timeBar = new TimeBar(date, price, volume);
                if(init==0) {
                    firstBar = timeBar;
                    currentBar = firstBar;
                    init++;
                } else {
                    currentBar.setNextBar(timeBar);
                    timeBar.setPreviousBar(currentBar);
                    currentBar = timeBar;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return firstBar;
    }

    // test utility
    public static void printVolumeBars(List<VolumeBar> volumeBars) {
        for(VolumeBar vbar : volumeBars) {
            System.out.println("Volume Bar");
            for(TimeBar tbar: vbar.getTimeBars()) {
                System.out.println(String.format("Trade Time: %s, Volume: %f, Price: %f", tbar.getTradeTime().toString(), tbar.getVolume(), tbar.getPrice()));
            }
        }
    }

    public static String printDateTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd,hh:mm:ss");
        return format.format(date);
    }

    public static void generateCsvFile(List<VPINBar> vpinBars, String directory, String ticker, int vbarAmount, int vpinBarSize) {
        try {
            String fileName = String.format("%s_%d_%d.csv", ticker, vbarAmount, vpinBarSize);
            String fileFullPath = String.format("%s\\%s", directory, fileName);
            FileWriter writer = new FileWriter(fileFullPath);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            for(VPINBar vpinBar : vpinBars) {
                writer.append(dateFormat.format(vpinBar.getEndTime()));
                writer.append(',');
                writer.append(timeFormat.format(vpinBar.getEndTime()));
                writer.append(',');
                writer.append(String.format("%.5f",vpinBar.getVpin()));
                writer.append(',');
                writer.append(String.format("%.2f",vpinBar.getLastTimeBarPrice()));
                writer.append('\n');
            }
            writer.flush();;
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

}
