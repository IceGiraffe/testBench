package site.ycsb.bayes;

import java.util.ArrayList;
/**
 * process data.
 */
public class PreRead {

  public ArrayList<ArrayList<String>> process(ArrayList<ArrayList<String>> llist, boolean type){
    ArrayList<ArrayList<String>> outList = new ArrayList<ArrayList<String>>();
    for(int i=0; i<llist.size(); i++){
      ArrayList<String> list=llist.get(i);
      if(type && list.contains("?")){ //
        continue;
      }
      ArrayList<String> tlist = new ArrayList<String>();
      tlist.add(String.valueOf(ageConversion(list.get(0))));
      tlist.add(String.valueOf(workclassConversion(list.get(1)))); //workclass
      tlist.add(String.valueOf(educationConversion(list.get(3)))); //education
      tlist.add(String.valueOf(maritalStatusConversion(list.get(5)))); //marital_status
      tlist.add(String.valueOf(occupationConversion(list.get(6)))); //occupation
      tlist.add(String.valueOf(relationshipConversion(list.get(7)))); //relationship
      tlist.add(String.valueOf(raceConversion(list.get(8)))); //race
      tlist.add(String.valueOf(sexConversion(list.get(9)))); //sex
      tlist.add(String.valueOf(hoursPerWeekConversion(list.get(12)))); //hours-per-wee
      tlist.add(String.valueOf(nativeCountryConversion(list.get(13)))); //nativeCountry
      tlist.add(resultConversion(list.get(14)));  
      outList.add(tlist);
    }
    return outList;
  }
  
  private int ageConversion(String string) {
    if (!string.contains("?")) {
      int ageTemp = Integer.parseInt(string);
      if (ageTemp<10) {
        return 1;
      } else if (ageTemp<20) {
        return 2;
      } else if (ageTemp<30) {
        return 3;
      } else if (ageTemp<40) {
        return 4;
      } else if (ageTemp<50) {
        return 5;
      } else if (ageTemp<60) {
        return 6;
      } else if (ageTemp<70) {
        return 7;
      } else if (ageTemp<80) {
        return 8;
      } else if (ageTemp<90) {
        return 9;
      } else {
        return 10;
      }
    }
    return 0;
  }
  private int workclassConversion(String string) {
    if (!string.contains("?")) {
      if (string.contains("Private")) {
        return 1;
      } else if (string.contains("Self-emp-not-inc")) {
        return 2;
      } else if (string.contains("Self-emp-inc")) {
        return 3;
      } else if (string.contains("Federal-gov")) {
        return 4;
      } else if (string.contains("Local-gov")) {
        return 5;
      } else if (string.contains("State-gov")) {
        return 6;
      } else if (string.contains("Without-pay")) {
        return 7;
      } else if (string.contains("Never-worked")) {
        return 8;
      }
    }
    return 0;
  }
  
  private int educationConversion(String string) {
    if (!string.contains("?")) {
      if (string.contains("Bachelors")) {
        return 1;
      } else if (string.contains("Some-college")) {
        return 2;
      } else if (string.contains("11th")) {
        return 3;
      } else if (string.contains("HS-grad")) {
        return 4;
      } else if (string.contains("Prof-school")) {
        return 5;
      } else if (string.contains("Assoc-acdm")) {
        return 6;
      } else if (string.contains("Assoc-voc")) {
        return 7;
      } else if (string.contains("9th")) {
        return 8;
      } else if (string.contains("7th-8th")) {
        return 9;
      } else if (string.contains("12th")) {
        return 10;
      } else if (string.contains("Masters")) {
        return 11;
      } else if (string.contains("1st-4th")) {
        return 12;
      } else if (string.contains("10th")) {
        return 13;
      } else if (string.contains("Doctorate")) {
        return 14;
      } else if (string.contains("5th-6th")) {
        return 15;
      } else if (string.contains("Preschool")) {
        return 16;
      }
    }
    return 0;
  }
  
  private int maritalStatusConversion(String string) {
    if (!string.contains("?")) {
      if (string.contains("Married-civ-spouse")) {
        return 1;
      } else if (string.contains("Divorced")) {
        return 2;
      } else if (string.contains("Never-married")) {
        return 3;
      } else if (string.contains("Separated")) {
        return 4;
      } else if (string.contains("Widowed")) {
        return 5;
      } else if (string.contains("Married-spouse-absent")) {
        return 6;
      } else if (string.contains("Married-AF-spouse")) {
        return 7;
      }
    }
    return 0; 
  }
  //occupation: Tech-support, Craft-repair, Other-service, Sales, Exec-managerial,
  //Prof-specialty, Handlers-cleaners, Machine-op-inspct, Adm-clerical, Farming-fishing,
  //Transport-moving, Priv-house-serv, Protective-serv, Armed-Forces.

  private int occupationConversion(String string) {
    if (!string.contains("?")) {
      if (string.contains("Tech-support")) {
        return 1;
      } else if (string.contains("Craft-repair")) {
        return 2;
      } else if (string.contains("Other-service")) {
        return 3;
      } else if (string.contains("Sales")) {
        return 4;
      } else if (string.contains("Exec-managerial")) {
        return 5;
      } else if (string.contains("Prof-specialty")) {
        return 6;
      } else if (string.contains("Handlers-cleaners")) {
        return 7;
      } else if (string.contains("Machine-op-inspct")) {
        return 8;
      } else if (string.contains("Adm-clerical")) {
        return 9;
      } else if (string.contains("Farming-fishing")) {
        return 10;
      } else if (string.contains("Transport-moving")) {
        return 11;
      } else if (string.contains("Priv-house-serv")) {
        return 12;
      } else if (string.contains("Protective-serv")) {
        return 13;
      } else if (string.contains("Armed-Forces")) {
        return 14;
      }
    }
    return 0;
  }
  //relationship: Wife, Own-child, Husband, Not-in-family, Other-relative, Unmarried.
  private int relationshipConversion(String string) {
    if (!string.contains("?")) {
      if (string.contains("Wife")) {
        return 1;
      } else if (string.contains("Own-child")) {
        return 2;
      } else if (string.contains("Husband")) {
        return 3;
      } else if (string.contains("Not-in-family")) {
        return 4;
      } else if (string.contains("Other-relative")) {
        return 5;
      } else if (string.contains("Unmarried")) {
        return 6;
      }
    }
    return 0; 
  }
//  race: White, Asian-Pac-Islander, Amer-Indian-Eskimo, Other, Black.
  private int raceConversion(String string) {
    if (!string.contains("?")) {
      if (string.contains("White")) {
        return 1;
      } else if (string.contains("Asian-Pac-Islander")) {
        return 2;
      } else if (string.contains("Amer-Indian-Eskimo")) {
        return 3;
      } else if (string.contains("Other")) {
        return 4;
      } else if (string.contains("Black")) {
        return 5;
      }
    }
    return 0; 
  }
  
  private int sexConversion(String string) {
    if (!string.contains("?")) {
      if (string.contains("Female")) {
        return 1;
      } else if (string.contains("Male")) {
        return 2;
      }
    }
    return 0; 
  }
  private int hoursPerWeekConversion(String string) {
    if (!string.contains("?")) {
      int workHourTemp=0;
      
      try {
        workHourTemp = Integer.parseInt(string);
      }catch(NumberFormatException n) {
        //n.printStackTrace();
        workHourTemp = Integer.parseInt(string.substring(1));
      }
      if (workHourTemp<10) {
        return 1;
      } else if (workHourTemp<20) {
        return 2;
      } else if (workHourTemp<30) {
        return 3;
      } else if (workHourTemp<40) {
        return 4;
      } else if (workHourTemp<50) {
        return 5;
      } else if (workHourTemp<60) {
        return 6;
      } else if (workHourTemp<70) {
        return 7;
      } else if (workHourTemp<80) {
        return 8;
      } else {
        return 9;
      }
    }
    return 0;
  }
  private int nativeCountryConversion(String string) {
    if (!string.contains("?")) {
      if (string.contains("United-States")) {
        return 1;
      } else {
        return 0;
      }
    }
    return 0; 
  }
  private String resultConversion(String string) {
    if (string.contains(">50K")) {
      return "yes";
    } else {
      return "no";
    }
  }
}
