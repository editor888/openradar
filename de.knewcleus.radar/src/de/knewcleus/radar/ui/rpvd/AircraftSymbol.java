package de.knewcleus.radar.ui.rpvd;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Deque;
import java.util.Iterator;
import java.util.logging.Logger;

import de.knewcleus.fgfs.Units;
import de.knewcleus.fgfs.location.Ellipsoid;
import de.knewcleus.fgfs.location.GeodToCartTransformation;
import de.knewcleus.fgfs.location.ICoordinateTransformation;
import de.knewcleus.fgfs.location.IDeviceTransformation;
import de.knewcleus.fgfs.location.Position;
import de.knewcleus.fgfs.location.Quaternion;
import de.knewcleus.fgfs.location.Vector3D;
import de.knewcleus.radar.autolabel.LabeledObject;
import de.knewcleus.radar.autolabel.PotentialGradient;
import de.knewcleus.radar.ui.Palette;
import de.knewcleus.radar.ui.aircraft.AircraftState;

public class AircraftSymbol implements LabeledObject {
	protected final static Logger logger=Logger.getLogger("de.knewcleus.rada.ui.rpvd");
	protected static final float aircraftSymbolSize=6.0f;
	private static final GeodToCartTransformation geodToCartTransformation=new GeodToCartTransformation(Ellipsoid.WGS84);

	protected static final double EPSILON=1E-10;
	
	protected static final double potAngleMax=5E2;
	protected static final double angle0=0.75*Math.PI;
	protected static final double angleMax=0.3*Math.PI;

	protected static final double potDistanceMax=5E2;
	protected static final double minLabelDist=10;
	protected static final double maxLabelDist=100;
	protected static final double meanLabelDist=(minLabelDist+maxLabelDist)/2.0;
	protected static final double labelDistRange=(maxLabelDist-minLabelDist);

	protected final RadarPlanViewContext radarPlanViewContext;
	protected final AircraftState aircraftState;
	protected final AircraftLabel label;
	
	protected Point2D currentDevicePosition;
	protected Point2D currentDeviceHeadPosition;
	protected final String labelLine[]=new String[5];
	protected boolean isLabelLocked=false;
	
	public AircraftSymbol(RadarPlanViewContext radarPlanViewContext, AircraftState aircraftState) {
		this.radarPlanViewContext=radarPlanViewContext;
		this.aircraftState=aircraftState;
		this.label=new AircraftLabel(this);
	}
	
	public AircraftState getAircraftState() {
		return aircraftState;
	}
	
	public RadarPlanViewContext getRadarPlanViewContext() {
		return radarPlanViewContext;
	}
	
	public Point2D getCurrentDevicePosition() {
		return currentDevicePosition;
	}
	
	public boolean canSelect() {
		return aircraftState.canSelect();
	}

	public void layout() {
		logger.fine("Laying out symbol for aircraft state "+aircraftState);
		final ICoordinateTransformation mapTransformation=radarPlanViewContext.getRadarPlanViewSettings().getMapTransformation();
		final IDeviceTransformation deviceTransformation=radarPlanViewContext.getDeviceTransformation();

		/* Calculate current device position of associatedTarget symbol */
		final Position currentGeodPosition=aircraftState.getPosition();
		final Position currentMapPosition=mapTransformation.forward(currentGeodPosition);
		currentDevicePosition=deviceTransformation.toDevice(currentMapPosition);

		/* Calculate current device position of leading line head position */
		final Position currentGeocPosition=geodToCartTransformation.forward(currentGeodPosition);
		final double trueCourseRad=aircraftState.getTrueCourse()/Units.RAD;
		final Vector3D courseVectorHF=new Vector3D(Math.cos(trueCourseRad),Math.sin(trueCourseRad),0.0);
		final Quaternion hf2gcf=Quaternion.fromLatLon(currentGeodPosition.getY(), currentGeodPosition.getX());
		final Vector3D courseVectorGCF=hf2gcf.transform(courseVectorHF);
		
		final double dt=radarPlanViewContext.getRadarPlanViewSettings().getSpeedVectorMinutes()*Units.MIN;
		final double distanceMade=aircraftState.getGroundSpeed()*dt;
		final Vector3D headingVector=courseVectorGCF.scale(distanceMade);
		final Position currentLeadingLineHeadPosition=currentGeocPosition.add(headingVector);
		final Position currentLeadingLineHeadGeodPosition=geodToCartTransformation.backward(currentLeadingLineHeadPosition);
		final Position currentLeadingLineHeadMapPosition=mapTransformation.forward(currentLeadingLineHeadGeodPosition);
		currentDeviceHeadPosition=deviceTransformation.toDevice(currentLeadingLineHeadMapPosition);
		
		label.updateLabelContents();
		label.layout();
		
		label.move(0,0);
	}
	
	public void drawSymbol(Graphics2D g2d) {
		g2d.setColor(aircraftState.getTaskState().getSymbolColor());
		Ellipse2D symbol=new Ellipse2D.Double(
				currentDevicePosition.getX()-aircraftSymbolSize/2.0,currentDevicePosition.getY()-aircraftSymbolSize/2.0,
				aircraftSymbolSize,aircraftSymbolSize);
		g2d.fill(symbol);
	}
	
	public void drawLabel(Graphics2D g2d) {
		double devX=currentDevicePosition.getX();
		double devY=currentDevicePosition.getY();
		
		double leStartX=devX;
		double leStartY=devY;
		double leEndX=leStartX+label.getHookX();
		double leEndY=leStartY+label.getHookY();
		g2d.setColor(Palette.WHITE);
		Line2D leadingLine=new Line2D.Double(leStartX,leStartY,leEndX,leEndY);
		g2d.draw(leadingLine);
		
		label.paint(g2d);
	}
	
	public void drawHeadingVector(Graphics2D g2d) {
		g2d.setColor(aircraftState.getTaskState().getSymbolColor());
		Line2D headingVector=new Line2D.Double(currentDevicePosition,currentDeviceHeadPosition);
		g2d.draw(headingVector);
	}
	
	public void drawTrail(Graphics2D g2d) {
		Position mapPos;
		Point2D devicePos;

		final Deque<Position> positionBuffer=aircraftState.getPositionBuffer();
		/* Draw the trail with previous positions */
		int dotCount=Math.min(positionBuffer.size()-1,radarPlanViewContext.getRadarPlanViewSettings().getTrackHistoryLength());
		if (dotCount<1)
			return;
		Color symbolColor=aircraftState.getTaskState().getSymbolColor();
		float alphaIncrease=1.0f/(dotCount+1);
		float alpha=1.0f;
		
		Iterator<Position> positionIterator=positionBuffer.descendingIterator();
		positionIterator.next(); // skip current position
		for (int i=0;i<dotCount;i++) {
			assert(positionIterator.hasNext());
			Position position=positionIterator.next();
			alpha-=alphaIncrease;
			mapPos=radarPlanViewContext.getRadarPlanViewSettings().getMapTransformation().forward(position);
			devicePos=radarPlanViewContext.getDeviceTransformation().toDevice(mapPos);
			g2d.setColor(new Color(symbolColor.getRed(),symbolColor.getGreen(),symbolColor.getBlue(),(int)(symbolColor.getAlpha()*alpha)));
			Ellipse2D symbol=new Ellipse2D.Double(devicePos.getX()-aircraftSymbolSize/2.0,devicePos.getY()-aircraftSymbolSize/2.0,
					aircraftSymbolSize,aircraftSymbolSize);
			g2d.fill(symbol);
		}
	}

	@Override
	public double getTop() {
		return currentDevicePosition.getY()-aircraftSymbolSize/2.0;
	}
	
	@Override
	public double getBottom() {
		return currentDevicePosition.getY()+aircraftSymbolSize/2.0;
	}
	
	@Override
	public double getLeft() {
		return currentDevicePosition.getX()-aircraftSymbolSize/2.0;
	}
	
	@Override
	public double getRight() {
		return currentDevicePosition.getX()+aircraftSymbolSize/2.0;
	}
	
	@Override
	public double getChargeDensity() {
		return 1;
	}
	
	public boolean containsPosition(double x, double y) {
		if (currentDevicePosition==null) {
			return false;
		}
		if (label.containsPosition(x, y)) {
			return true;
		}
		
		double dx,dy;
		
		dx=x-currentDevicePosition.getX();
		dy=y-currentDevicePosition.getY();
		
		if ((dx*dx+dy*dy)<=aircraftSymbolSize*aircraftSymbolSize) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isLocked() {
		return aircraftState.isSelected() || isLabelLocked;
	}
	
	public void setLocked(boolean isLocked) {
		this.isLabelLocked = isLocked;
	}
	
	@Override
	public AircraftLabel getLabel() {
		return label;
	}
	
	@Override
	public PotentialGradient getPotentialGradient(double dx, double dy) {
		final double vx,vy;
		
		vx=currentDeviceHeadPosition.getX()-currentDevicePosition.getX();
		vy=currentDeviceHeadPosition.getY()-currentDevicePosition.getY();
		
		final double dvx,dvy; // position relative to the trueCourse vector
		
		final double r=Math.sqrt(dx*dx+dy*dy);
		final double v=Math.sqrt(vx*vx+vy*vy);

		dvx=dx*vx+dy*vy;
		dvy=dx*vy-dy*vx;
		
		/*
		 * Calculate the angle contribution to the gradient.
		 * 
		 * The angle contribution to the weight is found as follows
		 * 
		 * w=wmax*((angle-a0)/amax)^2
		 * angle=abs(atan(y/x))
		 */
		
		// d/dt atan(t)=cos^2(atan(t))
		
		// dw/dx=wmax * d/dx ((angle-a0)/amax)^2
		// dw/dy=wmax * d/dy ((angle-a0)/amax)^2
		// d/dx ((angle-a0)/amax)^2 = 2*(angle-a0)/amax^2 * d/dx angle
		// d/dy ((angle-a0)/amax)^2 = 2*(angle-a0)/amax^2 * d/dy angle
		
		// d/dx angle=d/dx abs(atan(y/x)) = (d/dx atan(y/x)) * d/da abs(a) | a=atan(y/x)
		// d/dy angle=d/dy abs(atan(y/x)) = (d/dy atan(y/x)) * d/da abs(a) | a=atan(y/x)
		
		// d/dx atan(y/x) = (d/dx y/x) * d/dt atan(t) | t=y/x = -y/x^2 * cos^2(atan(y/x))
		// d/dy atan(y/x) = (d/dy y/x) * d/dt atan(t) | t=y/x =  1/x   * cos^2(atan(y/x))
		
		// dw/dx = 2 * wmax * (angle-a0)/amax^2 * sig(atan(y/x)) * cos^2(atan(y/x) * (-y/x^2)
		// dw/dy = 2 * wmax * (angle-a0)/amax^2 * sig(atan(y/x)) * cos^2(atan(y/x)) * 1/x

		final double angleForceX,angleForceY;
		
		if (abs(dvx)<EPSILON) {
			angleForceX=angleForceY=0.0;
		} else {
			final double angle=abs(Math.atan2(dvy,dvx));
			
			final double cangle=Math.cos(angle);
			final double cangle2=cangle*cangle;
			
			final double angleForce;
			
			angleForce=2.0 * potAngleMax * (angle-angle0)/(angleMax*angleMax) * signum(angle) * cangle2;
			
			final double angleForceVX, angleForceVY;
			
			angleForceVX=-dvy * angleForce / (dvx*dvx);
			angleForceVY=       angleForce / dvx;
			
			/* Transform from velocity relative frame to global frame */
			angleForceX=(vx*angleForceVX+vy*angleForceVY)/v;
			angleForceY=(vy*angleForceVX-vx*angleForceVY)/v;
		}
		
		/*
		 * Calculate the distance contribution.
		 * 
		 * w=wmax*(4*((r-rmean)/rd)^2-1)
		 */
		
		// dw/dx = dr/dx * dw/dr
		// dw/dy = dr/dy * dw/dr
		// dw/dr = 8*wmax*(r-rmean)/rd^2
		// dr/dx = x/r
		// dr/dy = y/r
		
		final double distanceForce;
		
		distanceForce=-8.0*potDistanceMax*(r-meanLabelDist)/(labelDistRange*labelDistRange);
		
		final double distanceForceX,distanceForceY;
		
		distanceForceX=dx/r*distanceForce;
		distanceForceY=dy/r*distanceForce;
		
		return new PotentialGradient(angleForceX+distanceForceX,angleForceY+distanceForceY);
	}
}
