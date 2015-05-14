import javax.print.attribute.standard.DateTimeAtCompleted;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Xiao Ye
 * Date: 4/23/15
 * Time: 2:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeBar {
    Date tradeTime;
    double price;
    double deltaPrice;
    double volume;
    double buy;
    double sell;
    double OI;
    TimeBar nextBar;
    TimeBar previousBar;

    public TimeBar(Date tradeTime, double price, int volume) {
        this.tradeTime = tradeTime;
        this.price = price;
        this.volume = volume;
    }

    public TimeBar() {}
	
    public Date getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(Date tradeTime) {
        this.tradeTime = tradeTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getDeltaPrice() {
        return deltaPrice;
    }

    public void setDeltaPrice(double deltaPrice) {
        this.deltaPrice = deltaPrice;
    }

    public TimeBar getNextBar() {
        return nextBar;
    }

    public void setNextBar(TimeBar nextBar) {
        this.nextBar = nextBar;
    }

    public TimeBar getPreviousBar() {
        return previousBar;
    }

    public void setPreviousBar(TimeBar previousBar) {
        this.previousBar = previousBar;
    }

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

    public double getOI() {
        return OI;
    }

    public void setOI(double OI) {
        this.OI = OI;
    }
}
