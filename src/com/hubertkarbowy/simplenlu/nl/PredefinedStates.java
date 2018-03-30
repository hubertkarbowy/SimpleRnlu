package com.hubertkarbowy.simplenlu.nl;
import com.hubertkarbowy.simplenlu.nl.SimpleRnlu;

import static com.hubertkarbowy.simplenlu.nl.SimpleRnlu.*;

public class PredefinedStates {

    public static void setDefaultContext (SimpleRnlu sessionClass) {
        sessionClass.setContext("here*Krakow");
        sessionClass.setContext("today*22.03.2018");
    }
}
