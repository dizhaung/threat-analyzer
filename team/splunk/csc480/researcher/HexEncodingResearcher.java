package team.splunk.csc480.researcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import team.splunk.csc480.data.DataItem;
import team.splunk.csc480.handler.ThreatHandler.Threat;
import team.splunk.csc480.handler.ThreatHandler.ThreatLevel;

public class HexEncodingResearcher extends HTTPResearcher {

	public HexEncodingResearcher(String key) {
		super(key);
		addKnowledge("GET .*%252E%252E%252F.* HTTP");
	}

	@Override
	public void reportEvent(DataItem item) {
		String ipAddress = "";
		String time = "";
		DateFormat df = null;
		Date curDate;

		if (item.fromFile.equals("access_log") || item.fromFile.equals("hex_log")) {
			Pattern ipPat = Pattern.compile("\\d+\\.\\d+\\.\\d+.\\d");
			Matcher ipMatch = ipPat.matcher(item.data);

			ipAddress = ipMatch.find() ? ipMatch.group(0) : "";

			Pattern timePat = Pattern.compile("\\d+/[A-Z][a-z][a-z]/\\d+:\\d+:\\d+:\\d+");
			Matcher timeMatch = timePat.matcher(item.data);

			time = timeMatch.find() ? timeMatch.group(0) : "";
			df = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss");
		}

		try {
			curDate = df.parse(time);
		} catch (Exception e) {
			return;
		}

		if (inferThreat(item, "GET .*%252E%252E%252F.* HTTP")) {
			this.reportThreat(new Threat(ipAddress, curDate.getTime(), item, 
					ThreatLevel.RED));
		}
	}
}
