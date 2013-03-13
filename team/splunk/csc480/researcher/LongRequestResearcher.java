package team.splunk.csc480.researcher;

import team.splunk.csc480.data.DataItem;
import team.splunk.csc480.handler.ThreatHandler.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Date;

public class LongRequestResearcher extends Researcher {
  private static final int kRed    = 10000000;
  private static final int kOrange = 7000000;
  private static final int kYellow = 3000000;

  private static final int kMinLogsForAverage = 50;
  private static final int kMinMicroseconds = 5000;

  private ArrayList<Double> accessTimes;
  private int logCount;

  public LongRequestResearcher(String key) {
    super(key);
    accessTimes = new ArrayList<Double>();
    logCount = 0;
  }

  private double getAverage() {
    double sum = 0;
    for (double time : accessTimes)
      sum += time;
    return sum / (double)accessTimes.size();
  }

  private double getStandardDeviation() {
    double average = getAverage();
    double sum = 0;
    for (double time : accessTimes)
      sum += Math.pow(average - time, 2);
    return Math.sqrt(sum / accessTimes.size());
  }

  private ThreatLevel getThreatLevel(double microseconds) {
    if (logCount > kMinLogsForAverage) {
      double average = getAverage();
      double sigma = getStandardDeviation();
      if (microseconds > accessTimes.get(accessTimes.size() - 1) + 0.5 * sigma)
        return ThreatLevel.RED;
      if (microseconds > average + 1.3 * sigma)
        return ThreatLevel.ORANGE;
      if (microseconds > average + 0.8 * sigma)
        return ThreatLevel.YELLOW;
      return ThreatLevel.BLUE;
    } else {
      if (microseconds > kRed)
        return ThreatLevel.RED;
      if (microseconds > kOrange)
        return ThreatLevel.ORANGE;
      if (microseconds > kYellow)
        return ThreatLevel.YELLOW;
    }
    return ThreatLevel.BLUE;
  }

  @Override
  public void reportEvent(DataItem item) {
    String usString;
    String ipAddress;
    String dateString;
    Date date = new Date();
    int microseconds;

    Pattern timePattern = Pattern.compile(".* \\d+ \\d+ (\\d+)$");
    Matcher timeMatcher = timePattern.matcher(item.data);
    usString = timeMatcher.find() ? timeMatcher.group(1) : "";
    if (usString.length() == 0)
      return;
    microseconds = Integer.parseInt(usString);

    Pattern ipPattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+) .*");
    Matcher ipMatcher = ipPattern.matcher(item.data);
    ipAddress = ipMatcher.find() ? ipMatcher.group(1) : "";

    Pattern datePattern = Pattern.compile(".*\\[(.*)\\].*");
    Matcher dateMatcher = datePattern.matcher(item.data);
    dateString = dateMatcher.find() ? dateMatcher.group(1) : "";
    SimpleDateFormat dateFormat = new SimpleDateFormat(
        "dd/MMM/yyyy:HH:mm:ss Z");

    try {
      date = dateFormat.parse(dateString);
    } catch (Exception e) { }

    ThreatLevel threatLevel = getThreatLevel(microseconds);
    if (threatLevel != ThreatLevel.BLUE) {
      this.reportThreat(
          new Threat(ipAddress, date.getTime(), item, threatLevel));
      return;
    }

    if (microseconds < kMinMicroseconds)
      return;

    ++logCount;
    accessTimes.add(new Double(microseconds));
  }
}
