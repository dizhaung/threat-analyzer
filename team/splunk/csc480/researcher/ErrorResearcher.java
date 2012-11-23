package team.splunk.csc480.researcher;

import team.splunk.csc480.data.DataItem;
import team.splunk.csc480.handler.ThreatHandler;
import team.splunk.csc480.handler.ThreatHandler.*;


import java.util.HashMap;

/**
 * Analyzes DataItems, looking for "warn", "error", etc.
 * @author Team Splunk (Austin Dworaczyk Wiltshire)
 * @version 1.0
 * @date November 20th, 2012
 */
public class ErrorResearcher extends Researcher {
   private static final HashMap<String, ThreatHandler.ThreatLevel> APACHEMAPPINGS = new HashMap<String, ThreatHandler.ThreatLevel>(){
      {
         put("warn", ThreatLevel.YELLOW);
         put("error", ThreatLevel.ORANGE);
         put("alert", ThreatLevel.RED);
      }
   };

   public ErrorResearcher() { }

   @Override
   public void reportEvent(DataItem item) {
      Threat t = null;

      //The below is just a place holder. I need to determine the layout of the DataItem data before
      //I can actually make something useful.
      for (String error : APACHEMAPPINGS.keySet()){
         /*if (item.data.containsKey(error)){
            t = new Threat("0.0.0.0", item, APACHEMAPPINGS.get(error));
         }*/
      }
      if (t != null){
         handler.reportThreat(t);
      }
   }
}
