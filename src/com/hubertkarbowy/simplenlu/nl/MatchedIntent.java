package com.hubertkarbowy.simplenlu.nl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class MatchedIntent {
    String intent;
    List<String> slots = new ArrayList<>();
    List<String> values = new ArrayList<>();

    public MatchedIntent (List<String> nlTokens, List<String> fstOutput, Locale culture, Map<String, String> clientContext) {
        int nlIndex=0;
        for (int oSymbol=0; oSymbol<fstOutput.size(); oSymbol++) {
            String currentSymbol = fstOutput.get(oSymbol);
            if (currentSymbol.startsWith("<REWRITE_SLOT:")) {
                String optType = currentSymbol.split(":")[1];
                slots.add(optType.replaceAll(">", ""));
                values.add(nlTokens.get(nlIndex));
            }

            if (currentSymbol.startsWith("<LOCALE_ALIAS_COMPUTED_SLOT:")) {
                String optType = currentSymbol.split(":")[1];
                optType = optType.replaceAll(">", "");
                slots.add(optType);
                values.add(AliasedSlots.compute(optType, nlTokens.get(nlIndex), culture));
            }

            if (currentSymbol.startsWith("<COMPUTED_SLOT:")) {
                String optType = currentSymbol.split(":")[1];
                String opFun = currentSymbol.split(":")[2];
                String computedValue = nlTokens.get(nlIndex);

                optType = optType.replaceAll(">", "");
                opFun = opFun.replaceAll(">", "");
                // if (opFun.equals("NominalizeFromDative")) {
                //    computedValue = "suwon";equals
               //  }
                computedValue = ComputedSlots.compute(opFun, nlTokens.get(nlIndex), culture);
                slots.add(optType);
                values.add(computedValue);
            }
            if (currentSymbol.startsWith("<DEFAULT_SLOT:")) { // opTypes and compute returns should be locale-independent
                String optType = currentSymbol.split(":")[1];
                optType = optType.replaceAll(">", "");
                slots.add(optType);
                values.add(DefaultSlots.compute(optType, clientContext, culture));
            }

            if (!currentSymbol.startsWith("{INTENT:")) nlIndex++;
            else {
                intent = currentSymbol.replaceAll("\\{INTENT:", "").replaceAll("}", "");
                continue;
            }

        }
    }

    public void addToken(String token) { slots.add(token); }
    public void addValue(String value) { values.add(value); }
    public void setIntent(String newInt) { intent = newInt; }

    public String toString() {
        StringBuilder formattedSlots = new StringBuilder();
        for (int x=0; x<slots.size(); x++) {
            formattedSlots.append("<"+slots.get(x)+":"+values.get(x)+">");
        }

        // return "{INTENT:" + intent + "}" + "\nSlots = " + slots + "\nValues = " + values;
        return "{INTENT:" + intent + "}" + formattedSlots.toString();
    }
}
