/**
 * Copyright (C) 2012,2013 Wolfram Wagner
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
 * OpenRadar ist Freie Software: Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation, Version 3 der
 * Lizenz oder (nach Ihrer Option) jeder späteren veröffentlichten Version,
 * weiterverbreiten und/oder modifizieren.
 *
 * OpenRadar wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE
 * GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite Gewährleistung der
 * MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK. Siehe die GNU General
 * Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.knewcleus.openradar.gui.status;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.knewcleus.openradar.gui.GuiMasterController;
import de.knewcleus.openradar.gui.Palette;
import de.knewcleus.openradar.gui.setup.AirportData;

public class MetarSettingsDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final GuiMasterController master;

    private JTextField tfOwnWeatherStation = new JTextField();
    private JTextField tfAddWeatherStation = new JTextField();

    private TextFieldListener textFieldListener = new TextFieldListener();

    public MetarSettingsDialog(GuiMasterController master) {
        this.master = master;
        initComponents();
    }

    private void initComponents() {
        setTitle("OpenRadar - Flightplan");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        this.addWindowListener(new DialogCloseListener());

        // Determine what the default GraphicsDevice can support.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        boolean isUniformTranslucencySupported = gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT);
        if(isUniformTranslucencySupported) {
            this.setOpacity(0.92f);
        }

        List<Image> icons = new ArrayList<Image>();
        File iconDir = new File("res/icons");
        if(iconDir.exists()) {
            File[] files = iconDir.listFiles();
            for(File f : files) {
                if(f.getName().matches("OpenRadar.*\\.ico") || f.getName().matches("OpenRadar.*\\.png")
                  || f.getName().matches("OpenRadar.*\\.gif") || f.getName().matches("OpenRadar.*\\.jpg")) {
                    icons.add(new ImageIcon(f.getAbsolutePath()).getImage());
                }
            }
            if(!icons.isEmpty()) {
                setIconImages(icons);
            }
        }

        setLayout(new GridBagLayout());

        setForeground(Palette.DESKTOP_TEXT);
        setBackground(Palette.DESKTOP);

        JLabel lbOwnWeatherStation = new JLabel("Weather station");
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 4, 8);
        add(lbOwnWeatherStation, gridBagConstraints);

        tfOwnWeatherStation.setToolTipText("nearest weather station");
        tfOwnWeatherStation.addKeyListener(textFieldListener);
        Dimension preferredSize = tfAddWeatherStation.getPreferredSize();
        preferredSize.setSize(100, preferredSize.getHeight());
        tfOwnWeatherStation.setPreferredSize(preferredSize);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 4, 8);
        add(tfOwnWeatherStation, gridBagConstraints);

        JLabel lbAddWeatherStation = new JLabel("Add stations");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 4, 8);
        add(lbAddWeatherStation, gridBagConstraints);

        tfAddWeatherStation.setToolTipText("Comma separated list of additional weather stations to display");
        tfAddWeatherStation.addKeyListener(textFieldListener);
        preferredSize = tfAddWeatherStation.getPreferredSize();
        preferredSize.setSize(200, preferredSize.getHeight());
        tfAddWeatherStation.setPreferredSize(preferredSize);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 4, 8);
        add(tfAddWeatherStation, gridBagConstraints);

        pack();
    }

    public void show(MouseEvent e) {
        tfOwnWeatherStation.setText(master.getAirportData().getMetarSource());
        tfAddWeatherStation.setText(master.getAirportData().getAddMetarSources()!=null?master.getAirportData().getAddMetarSources():"");

        Dimension innerSize = getPreferredSize();
        setSize(new Dimension((int)innerSize.getWidth()+8, (int)innerSize.getHeight()+8));
        Rectangle maxBounds = AirportData.MAX_WINDOW_SIZE;

        Point2D p = e.getLocationOnScreen();// ((JComponent) e.getSource()).getLocationOnScreen();
        p = new Point2D.Double(p.getX() - this.getWidth() - 10, p.getY());

        int lowerDistanceToScreenBorder=50;
        if(p.getY()+getHeight()>maxBounds.getHeight()-lowerDistanceToScreenBorder) {
            p = new Point2D.Double(p.getX(), maxBounds.getHeight()-getHeight() - lowerDistanceToScreenBorder);
        }
        setLocation(new Point((int) p.getX(), (int) p.getY()));
        doLayout();
        setVisible(true);
        invalidate();
        tfOwnWeatherStation.requestFocus();
    }

    private class DialogCloseListener extends WindowAdapter {
        @Override
        public void windowClosed(WindowEvent e) {
            closeDialog();
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            closeDialog();
        }

        @Override
        public void windowLostFocus(WindowEvent e) {
            closeDialog();
        }

    }

    public void closeDialog() {
        if(isVisible()) {
            master.getMetarReader().changeMetarSources(tfOwnWeatherStation.getText(),tfAddWeatherStation.getText());
            master.getAirportData().storeAirportData(master);
            setVisible(false);
        }
    }

    private class TextFieldListener extends KeyAdapter {
        @Override
        public void keyTyped(KeyEvent e) {

            if(e.getKeyChar()==KeyEvent.VK_ENTER) {
                if(tfOwnWeatherStation.equals(e.getSource())) {
                    tfAddWeatherStation.requestFocus();
                }
                else if(tfAddWeatherStation.equals(e.getSource())) {
                    closeDialog();
                }
            } else if(e.getKeyChar()==KeyEvent.VK_ESCAPE) {
                closeDialog();
            }
        }
    }
}
