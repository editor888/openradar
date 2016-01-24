package de.knewcleus.openradar.gui.flightstrips.rules;

import java.util.ArrayList;

import org.jdom2.Element;

import de.knewcleus.openradar.gui.flightplan.FlightPlanData;
import de.knewcleus.openradar.gui.flightstrips.FlightStrip;

public class DestinationHereRule extends AbstractRule {

	private final boolean isLanding;
	
	public DestinationHereRule(boolean isLanding) {
		this.isLanding = isLanding;
	}
	
	@Override
	public boolean isAppropriate(FlightStrip flightstrip) {
		FlightPlanData flightplan = flightstrip.getContact().getFlightPlan();
		return (flightplan != null) && (flightplan.contactWillLandHere() == isLanding);
	}

	@Override
	public ArrayList<String> getRuleText() {
		ArrayList<String> result = new ArrayList<String>();
		result.add("contact is " + (isLanding ? "" : "not") + " landing here.");
		return result;
	}

	// --- DomAttributes ---
	
	@Override
	public void putAttributes(Element element) {
		super.putAttributes(element);
		element.setAttribute("islanding", String.valueOf(isLanding));
	}

}
