/**
 * Copyright (C) 2012 Wolfram Wagner 
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
package de.knewcleus.openradar.gui.chat.auto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import de.knewcleus.openradar.gui.GuiMasterController;
import de.knewcleus.openradar.gui.contacts.GuiRadarContact;
import de.knewcleus.openradar.gui.setup.AirportData;
import de.knewcleus.openradar.weather.MetarData;

public class AtcMessage {

    private final String displayMessage;
    private Map<String,String> translations = new TreeMap<String, String>();
    private List<String> variables = new ArrayList<String>();

    public AtcMessage(String displayMessage) {
        this.displayMessage=displayMessage;
    }
    
    public void setVariables(String variableList) {
        StringTokenizer st = new StringTokenizer(variableList,",");
        while (st.hasMoreElements()) {
            variables.add(st.nextToken().trim());
        }
    }

    public void addTranslation(String language, String text) {
        translations.put(language, text);
    }
    
    public String getDisplayMessage() {
        return displayMessage;
    }
    
    public List<String> generateMessages(GuiMasterController master, GuiRadarContact contact, String additionalLanguage) {
        List<String> result = new ArrayList<String>();

        result.add(replaceVariables(translations.get("en"), master, contact));

        if(additionalLanguage!=null) {
            if(!"en".equals(additionalLanguage)) {
                String localizedText = replaceVariables(translations.get(additionalLanguage), master, contact); 
                if(!localizedText.trim().isEmpty()) {
                    result.add(localizedText);
                }
            }
        }        
        return result;
    }

    private String replaceVariables(String text, GuiMasterController master, GuiRadarContact contact) {
        AirportData data = master.getDataRegistry();
        MetarData metar = master.getMetarReader().getMetar();
        
        /*  
         * 
         * /environment/pressure-sea-level-inhg,
         * /environment/wind-speed-kt
         * /instrumentation/comm/frequencies/selected-mhz
         * /sim/atc/activeRW
         * /sim/atc/wind-from-display
         * /sim/gui/dialogs/ATC-ML/ATC-MP/CMD-APalt
         * /sim/gui/dialogs/ATC-ML/ATC-MP/CMD-APname
         * /sim/gui/dialogs/ATC-ML/ATC-MP/CMD-target
         * /sim/tower/airport-id
         * /sim/gui/dialogs/ATC-ML/ATC-MP/CMD-target-range
         * 
         */
        ArrayList<Object> values = new ArrayList<Object>();
        for(String varName : variables) {
            if("/environment/pressure-sea-level-inhg".equals(varName)) {
                values.add(metar.getPressureInHG());
            } else if("/environment/wind-speed-kt".equals(varName)) {
                values.add((float)metar.getWindSpeed()); // todo add gusts
            } else if("/instrumentation/comm/frequencies/selected-mhz".equals(varName)) {
                if(master.getRadioManager().getModels().isEmpty()) {
                    values.add(new Double("0"));
                } else {
                    values.add(Double.parseDouble(master.getRadioManager().getModels().get("COM0").getSelectedItem().getFrequency())); // todo multiple frequencies?
                }
            } else if("/sim/atc/activeRW".equals(varName)) {
                values.add(master.getStatusManager().getActiveRunways()); 
            } else if("/sim/atc/wind-from-display".equals(varName)) {
                values.add((float)metar.getWindDirection()); // todo add variation
            } else if("/sim/gui/dialogs/ATC-ML/ATC-MP/CMD-APalt".equals(varName)) {
                values.add(data.getElevationFt()); 
            } else if("/sim/gui/dialogs/ATC-ML/ATC-MP/CMD-APname".equals(varName)) {
                values.add(data.getAirportName()); 
            } else if("/sim/gui/dialogs/ATC-ML/ATC-MP/CMD-target".equals(varName)) {
                values.add(master.getRadarContactManager().getSelectedContact().getCallSign()); 
            } else if("/sim/tower/airport-id".equals(varName)) {
                values.add(data.getAirportCode()); 
            } else if("/sim/gui/dialogs/ATC-ML/ATC-MP/CMD-target-range".equals(varName)) {
                values.add(contact.getRadarContactDistanceD()); 
            }
        }
        
        return String.format(text,values.toArray()).trim()+" ";
    }
    
    public String toString() {
        return displayMessage;
    }
}