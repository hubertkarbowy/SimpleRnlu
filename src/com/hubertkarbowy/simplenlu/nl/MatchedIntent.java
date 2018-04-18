package com.hubertkarbowy.simplenlu.nl;

import com.hubertkarbowy.simplenlu.nl.slots.AliasedSlots;
import com.hubertkarbowy.simplenlu.nl.slots.ComputedSlots;
import com.hubertkarbowy.simplenlu.nl.slots.DefaultSlots;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MatchedIntent {
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
                AliasedSlots as = AliasedSlots.getAliasedSlots(culture);
                String computedValue = AliasedSlots.compute(as.getAliases(), optType, nlTokens.get(nlIndex));
                if (computedValue==null) throw new RuntimeException("Aliased slot is null!");
                values.add(computedValue);
            }

            if (currentSymbol.startsWith("<COMPUTED_SLOT:")) {
                String optType = currentSymbol.split(":")[1];
                String opFun = currentSymbol.split(":")[2];
                String computedValue = nlTokens.get(nlIndex);

                optType = optType.replaceAll(">", "");
                opFun = opFun.replaceAll(">", "");
                computedValue = ComputedSlots.compute(opFun, optType, nlTokens.get(nlIndex), culture);
                if (computedValue==null) throw new RuntimeException("Computed slot is null!");
                slots.add(optType);
                values.add(computedValue);
            }
            if (currentSymbol.startsWith("<DEFAULT_SLOT:")) { // opTypes and compute returns should be locale-independent
                String optType = currentSymbol.split(":")[1];
                String opFun = currentSymbol.split(":")[2];
                opFun = opFun.replaceAll(">", "");
                optType = optType.replaceAll(">", "");
                String computedValue = DefaultSlots.compute(opFun, clientContext);
                if (computedValue==null) throw new RuntimeException("Default slot is null!");
                slots.add(optType);
                values.add(computedValue);
            }

            if (!currentSymbol.startsWith("{INTENT:")) nlIndex++;
            else {
                intent = currentSymbol.replaceAll("\\{INTENT:", "").replaceAll("}", "");
                continue;
            }

        }

        boolean nothingToMerge=false;
        while (!nothingToMerge) { // If there are two neighboring slots of the same type, we merge them
            nothingToMerge=true;
            for (int x=0; x<slots.size(); x++) {
                if (x==slots.size()-1) break;
                String token = slots.get(x);
                String nextToken = slots.get(x+1);
                // System.out.println(">> this = " + token + ", next = " + nextToken);
                if (token.equals(nextToken)) {
                    values.set(x, values.get(x) + " " + values.get(x+1));
                    values.remove(x+1);
                    slots.remove(x+1);
                    nothingToMerge=false;
                    break;
                }
            }
        }
    }

    public void addToken(String token) { slots.add(token); }
    public void addValue(String value) { values.add(value); }
    public void setIntent(String newInt) { intent = newInt; }

    public String getIntent() { return intent; }
    public List<String> getSlotsAndValues () {

        List<String> formattedSlots = new ArrayList<>();
        for (int x=0; x<slots.size(); x++) {
            String slotCumValue = "<" + slots.get(x) + ":" + values.get(x) + ">";
            formattedSlots.add(slotCumValue);
        }
        return formattedSlots;
    }

    public String toString() {
        StringBuilder formattedSlots = new StringBuilder();
        for (int x=0; x<slots.size(); x++) {
            formattedSlots.append("<"+slots.get(x));
            formattedSlots.append(":" + values.get(x) + ">");
        }

        // return "{INTENT:" + intent + "}" + "\nSlots = " + slots + "\nValues = " + values;
        return "{INTENT:" + intent + "}" + formattedSlots.toString();
    }
}
