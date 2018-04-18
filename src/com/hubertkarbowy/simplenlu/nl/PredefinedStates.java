package com.hubertkarbowy.simplenlu.nl;
import com.hubertkarbowy.simplenlu.nl.slots.AliasedSlots;

public class PredefinedStates {

    public static void setDefaultContext (SimpleRnlu sessionClass) {
        sessionClass.setContext("here*Cracow");
        AliasedSlots as = AliasedSlots.getAliasedSlots(sessionClass.locale);
        sessionClass.setContext("today*"+ AliasedSlots.compute(as.getAliases(), "NamedDate", "dziś")); // hmm...
    }
}
