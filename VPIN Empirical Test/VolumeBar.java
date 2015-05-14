import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Xiao Ye
 * Date: 4/23/15
 * Time: 2:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class VolumeBar {
    List<TimeBar> timeBars;
    Date startTime;
    Date endTime;
    double std;

    double buy;
    double sell;
    double orderImbalance;

    public double getBuy() {
        return buy;
    }

    public void setBuy(double buy) {
        this.buy = buy;
    }

    public double getSell() {
        return sell;
    }

    public void setSell(double sell) {
        this.sell = sell;
    }

    public double getOrderImbalance() {
        return orderImbalance;
    }

    public void setOrderImbalance(double orderImbalance) {
        this.orderImbalance = orderImbalance;
    }

    public VolumeBar() {
        timeBars = Lists.newArrayList();
    }

    public List<TimeBar> getTimeBars() {
        return timeBars;
    }

    public void setTimeBars(List<TimeBar> timeBars) {
        this.timeBars = timeBars;
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

    public double getStd() {
        return std;
    }

    public void setStd(double std) {
        this.std = std;
    }
}
