/**
 * Copyright (C) 2013 Wolfram Wagner
 *
 * This file is part of OpenRadar.
 *
 * OpenRadar is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenRadar is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenRadar. If not, see <http://www.gnu.org/licenses/>.
 *
 * Diese Datei ist Teil von OpenRadar.
 *
 * OpenRadar ist Freie Software: Sie k�nnen es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation, Version 3 der
 * Lizenz oder (nach Ihrer Option) jeder sp�teren ver�ffentlichten Version,
 * weiterverbreiten und/oder modifizieren.
 *
 * OpenRadar wird in der Hoffnung, dass es n�tzlich sein wird, aber OHNE JEDE
 * GEW�HELEISTUNG, bereitgestellt; sogar ohne die implizite Gew�hrleistung der
 * MARKTF�HIGKEIT oder EIGNUNG F�R EINEN BESTIMMTEN ZWECK. Siehe die GNU General
 * Public License f�r weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.knewcleus.openradar.gui.flightplan;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Element;

import de.knewcleus.openradar.gui.contacts.GuiRadarContact;
import de.knewcleus.openradar.gui.setup.AirportData;

/**
 *
 * @author Wolfram Wagner
 */
public class FpXml_1_0 {

    public static String getCallSign(Element eFlightPlan) {
        return eFlightPlan.getChild("header").getChildText("callsign");
    }

    public static FlightPlanData parseXml(AirportData airportData, GuiRadarContact contact, Element eFlightPlan) {
        /*
         * <header> <flight>unique number of flightplan</flight> <callsign>callsign of aiplane</callsign>
         * <owner>callsign of owning ATC</owner> <handover>optional callsign of next ATC</handover>
         * <squawk>assignedSquawk</squawk> <assignedAlt>XML decimal</assignedAlt>
         * <status>filed,active,closed,expired</status> </header> <data> <type>VFR/IFR</type> <aircraft>ICAO of
         * aircraft</aircraft> <trueAirspeed>travel air speed of aircraft</trueAirspeed> <departure>departure
         * airport</departure> <departureTime>XML date+time</departureTime> <cruisingAlt>XML decimal</cruisingAlt>
         * <route>comma separated list of navaid codes</route> <destination>destination airport</destination>
         * <alternateDest>comma separated alternative destination airports</alternateDest>
         * <estFlightTime>hours:minutes</estFlightTime> <fuelTime>hours:minutes</fuelTime> <pilot>Name of Pilot</pilot>
         * <sob>number of souls on board</sob> <remarks></remarks> </data>
         */

        // check if
        String flightCode = eFlightPlan.getChild("header").getChildText("flight");
        String callsign = eFlightPlan.getChild("header").getChildText("callsign");
        String owner = eFlightPlan.getChild("header").getChildText("owner");
        String handover = eFlightPlan.getChild("header").getChildText("handover");
        String squawk = eFlightPlan.getChild("header").getChildText("squawk");
        String assignedAlt = eFlightPlan.getChild("header").getChildText("assignedAlt");
        String state = eFlightPlan.getChild("header").getChildText("status");

        String type = eFlightPlan.getChild("data").getChildText("type");
        String aircraft = eFlightPlan.getChild("data").getChildText("aircraft");
        String trueAirspeed = eFlightPlan.getChild("data").getChildText("trueAirspeed");
        String departure = eFlightPlan.getChild("data").getChildText("departure");
        String departureTime = eFlightPlan.getChild("data").getChildText("departureTime");
        String cruisingAlt = eFlightPlan.getChild("data").getChildText("cruisingAlt");
        String route = eFlightPlan.getChild("data").getChildText("route");
        String destination = eFlightPlan.getChild("data").getChildText("destination");
        String alternateDest = eFlightPlan.getChild("data").getChildText("alternateDest");
        String estFlightTime = eFlightPlan.getChild("data").getChildText("estFlightTime");
        String fuelTime = eFlightPlan.getChild("data").getChildText("fuelTime");
        String pilot = eFlightPlan.getChild("data").getChildText("pilot");
        String soulsOnBoard = eFlightPlan.getChild("data").getChildText("soulsOnBoard");
        String remarks = eFlightPlan.getChild("data").getChildText("remarks");

        FlightPlanData fp;
        FlightPlanData existingFp = contact.getFlightPlan();

        if(existingFp!=null) {
            existingFp.update(flightCode, callsign, owner, handover, squawk, assignedAlt, state, type, aircraft, trueAirspeed, departure, departureTime,
                    cruisingAlt, route, destination, alternateDest, estFlightTime, fuelTime, pilot, soulsOnBoard, remarks);
            fp=existingFp;
        } else {
            fp = new FlightPlanData(airportData, contact, flightCode, callsign, owner, handover, squawk, assignedAlt, state, type, aircraft, trueAirspeed, departure, departureTime,
                                    cruisingAlt, route, destination, alternateDest, estFlightTime, fuelTime, pilot, soulsOnBoard, remarks);
        }
        return fp;
    }

    public static Element createXml(FlightPlanData fp) {
        /*
         * <header> <flight>unique number of flightplan</flight> <callsign>callsign of aiplane</callsign>
         * <owner>callsign of owning ATC</owner> <handover>optional callsign of next ATC</handover>
         * <squawk>assignedSquawk</squawk> <assignedAlt>XML decimal</assignedAlt>
         * <status>filed,active,closed,expired</status> </header> <data> <type>VFR/IFR</type> <aircraft>ICAO of
         * aircraft</aircraft> <trueAirspeed>travel air speed of aircraft</trueAirspeed> <departure>departure
         * airport</departure> <departureTime>XML date+time</departureTime> <cruisingAlt>XML decimal</cruisingAlt>
         * <route>comma separated list of navaid codes</route> <destination>destination airport</destination>
         * <alternateDest>comma separated alternative destination airports</alternateDest>
         * <estFlightTime>hours:minutes</estFlightTime> <fuelTime>hours:minutes</fuelTime> <pilot>Name of Pilot</pilot>
         * <sob>number of souls on board</sob> <remarks></remarks> </data>
         */
        Element eFlightPlan = new Element("flightplan");

        try {
            eFlightPlan.setAttribute("version", "1.0");

            // header

            Element eFpHeader = new Element("header");
            eFlightPlan.addContent(eFpHeader);

            Element eFlight = new Element("flight");
            if (fp.getFlightCode() != null) {
                eFlight.setText(fp.getFlightCode());
            }
            eFpHeader.addContent(eFlight);

            Element eCallsign = new Element("callsign");
            if (fp.getCallsign() != null) {
                eCallsign.setText(fp.getCallsign());
            }
            eFpHeader.addContent(eCallsign);

            Element eOwner = new Element("owner");
            if (fp.getOwner() != null) {
                eOwner.setText(fp.getOwner());
            }
            eFpHeader.addContent(eOwner);

            Element eHandover = new Element("handover");
            if (fp.getHandover() != null) {
                eHandover.setText(fp.getHandover());
            }
            eFpHeader.addContent(eHandover);

            Element eSquawk = new Element("squawk");
            if (fp.getSquawk() != null) {
                eSquawk.setText(fp.getSquawk());
            }
            eFpHeader.addContent(eSquawk);

            Element eAssignedAlt = new Element("assignedAlt");
            if (fp.getAssignedAltitude() != null) {
                eAssignedAlt.setText(fp.getAssignedAltitude());
            }
            eFpHeader.addContent(eAssignedAlt);

            Element eState = new Element("status");
            if (fp.getFpStatus() != null) {
                eState.setText(fp.getFpStatus().toString());
            }
            eFpHeader.addContent(eState);

            // data

            Element eFpData = new Element("data");
            eFlightPlan.addContent(eFpData);

            Element eType = new Element("type");
            if (fp.getType() != null) {
                eType.setText(fp.getType());
            }
            eFpData.addContent(eType);

            Element eAircraft = new Element("aircraft");
            if (fp.getAircraft() != null) {
                eAircraft.setText(fp.getAircraft());
            }
            eFpData.addContent(eAircraft);

            Element eTrueAirspeed = new Element("trueAirspeed");
            if (fp.getTrueAirspeed() != null) {
                eTrueAirspeed.setText(fp.getTrueAirspeed());
            }
            eFpData.addContent(eTrueAirspeed);

            Element eDeparture = new Element("departure");
            if (fp.getDeparture() != null) {
                eDeparture.setText(fp.getDeparture());
            }
            eFpData.addContent(eDeparture);

            Element eDepartureTime = new Element("departureTime");
            if (fp.getDeparture() != null) {
                eDepartureTime.setText(fp.getDeparture());
            }
            eFpData.addContent(eDepartureTime);

            Element eCruisingAlt = new Element("cruisingAlt");
            if (fp.getCruisingAltitude() != null) {
                eCruisingAlt.setText(fp.getCruisingAltitude());
            }
            eFpData.addContent(eCruisingAlt);

            Element eRoute = new Element("route");
            if (fp.getRoute() != null) {
                eRoute.setText(fp.getRoute());
            }
            eFpData.addContent(eRoute);

            Element eDestination = new Element("destination");
            if (fp.getDestinationAirport() != null) {
                eDestination.setText(fp.getDestinationAirport());
            }
            eFpData.addContent(eDestination);

            Element eAlternateDest = new Element("alternateDest");
            if (fp.getAlternativeDestinationAirports() != null) {
                eAlternateDest.setText(fp.getAlternativeDestinationAirports());
            }
            eFpData.addContent(eAlternateDest);

            Element eEstFlightTime = new Element("estFlightTime");
            if (fp.getEstimatedFlightTime() != null) {
                eEstFlightTime.setText(fp.getEstimatedFlightTime());
            }
            eFpData.addContent(eEstFlightTime);

            Element eFuelTime = new Element("fuelTime");
            if (fp.getEstimatedFuelTime() != null) {
                eFuelTime.setText(fp.getEstimatedFuelTime());
            }
            eFpData.addContent(eFuelTime);

            Element ePilot = new Element("pilot");
            if (fp.getPilotName() != null) {
                ePilot.setText(fp.getPilotName());
            }
            eFpData.addContent(ePilot);

            Element eSoulsOnBoard = new Element("soulsOnBoard");
            eSoulsOnBoard.setText(""+fp.getSoulsOnBoard());

            eFpData.addContent(eSoulsOnBoard);

            Element eRemarks = new Element("remarks");
            if (fp.getRemarks() != null) {
                eRemarks.setText(fp.getRemarks());
            }
            eFpData.addContent(eRemarks);

        } catch (Exception e) {
            Logger.getLogger(FpXml_1_0.class.toString()).log(Level.SEVERE,"Problem to create XML for flightplan " + fp.getFlightCode(), e);
            eFlightPlan = null;
        }
        return eFlightPlan;
    }
}