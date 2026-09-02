package meico.supplementary;

public class Stopwatch {
    private long startTime;
    private long lastMarkTime;
    private int reportingThreshold = 0;

    public Stopwatch() {
        reset();
    }
    public Stopwatch(String msg) {
        reset(msg);
    }
    public Stopwatch(String msg, int reportingThreshold) {
        setReportingThreshold(reportingThreshold);
        reset(msg);
    }

    public void setReportingThreshold(int reportingThreshold) {
        this.reportingThreshold = reportingThreshold;
    }

    public void reset() {
        reset("");
    }

    public void reset(String msg) {
        if(!msg.isEmpty())
            System.out.println("Stopwatch started: " + msg);
        else
            System.out.println("Stopwatch started.");
        startTime = System.currentTimeMillis();
        lastMarkTime = startTime;
    }

    public long mark() {
        return mark("");
    }

    public long mark(String msg) {
        long elapsedTime = elapsedMillis();
        if(elapsedTime > reportingThreshold)
            System.out.println(msg + " - Time consumed: " + elapsedTime + " milliseconds");
        lastMarkTime = System.currentTimeMillis();
        return elapsedTime;
    }

    public long markTotal() {
        return mark("");
    }

    public long markTotal(String msg) {
        long elapsedTime = elapsedTotalMillis();
        if(elapsedTime > reportingThreshold)
            System.out.println(msg + " - Total time consumed: " + elapsedTime + " milliseconds");
        return elapsedTime;
    }

    public long elapsedMillis() {
        return (System.currentTimeMillis() - lastMarkTime);
    }

    public long elapsedTotalMillis() {
        return (System.currentTimeMillis() - startTime);
    }
}

