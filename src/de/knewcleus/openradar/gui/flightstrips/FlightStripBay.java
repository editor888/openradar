package de.knewcleus.openradar.gui.flightstrips;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import de.knewcleus.openradar.gui.GuiMasterController;
import de.knewcleus.openradar.gui.Palette;

/* FlightStripBay is a visual component 
 * where the user will find the flight strips.
 * It contains sections to organize the flight strips
 */
public class FlightStripBay extends JPanel {

	private static final long serialVersionUID = 4074532865663077094L;

	private final SectionsPanel sectionspanel; 
	
	public FlightStripBay(GuiMasterController master) {
		super();
		setBackground(Palette.SECTION_BACKGROUND);
		setOpaque(true);
		setLayout(new GridBagLayout());
		// fills this panel with a scrollpane
		// add panel as a scrollable vertical list for the sections
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		// panel
		ScrollablePanel panel = new ScrollablePanel();
		JScrollPane scrollpane = new JScrollPane(panel);
		scrollpane.setOpaque(false);
		scrollpane.getViewport().setOpaque(false);
		add(scrollpane, gridBagConstraints);
		// sectionspanel top in panel
		sectionspanel = new SectionsPanel(master);
		panel.add(sectionspanel, BorderLayout.NORTH);
	}
	
	public void recreateContents() {
		sectionspanel.recreateContents();
		revalidate();
	}
	
	public SectionsPanel getSectionsPanel() {
		return sectionspanel;
	}
	
	// ===================================================================
	
	protected class ScrollablePanel extends JPanel implements Scrollable {
		
		private static final long serialVersionUID = 3280716992665711788L;

		public ScrollablePanel() {
			super();
			setLayout(new BorderLayout());
			setOpaque(true);
			setBackground(Palette.WHITE);
		}
		
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			// A component without any properties that would affect the viewport size should just return getPreferredSize here.
			return getPreferredSize();
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			// return true if a viewport should force the Scrollables height to match its own.
			return false;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			// return true if a viewport should force the Scrollables width to match its own.
			return true;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		
		
	}
	
}
