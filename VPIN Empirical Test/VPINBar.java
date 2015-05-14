import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Xiao Ye
 * Date: 4/25/15
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class VPINBar {
    Date startTime;
    Date endTime;
    Date midTime;
    double vpin;

    public double getLastTimeBarPrice() {
        return lastTimeBarPrice;
    }

    public void setLastTimeBarPrice(double lastTimeBarPrice) {
        this.lastTimeBarPrice = lastTimeBarPrice;
    }

    double lastTimeBarPrice;

    public VPINBar() {}

    public VPINBar(Date startTime, Date endTime, Date midTime, double vpin) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.midTime = midTime;
        this.vpin = vpin;
    }

    public Date getMidTime() {
        return midTime;
    }

    public void setMidTime(Date midTime) {
        this.midTime = midTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public double getVpin() {
        return vpin;
    }

    public void setVpin(double vpin) {
        this.vpin = vpin;
    }
}
