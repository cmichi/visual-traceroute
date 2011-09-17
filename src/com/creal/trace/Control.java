package com.creal.trace;

import com.*;

import java.util.Vector;

import controlP5.Range;
import controlP5.Textlabel;

/** 
 * Class for control objects.
 * 
 * @author cmichi
 */
public class Control {

	private Main p;

	/** align all controls to this x */
	public final int x = 40;

	/** current y for placing controls */
	public int yOriginal = 40 + 55;
	public int y = yOriginal;

	public controlP5.Textfield b;
	public int i = 0;

	/** collections for the control elements */
	public Vector<Textlabel> els_labels = new Vector<Textlabel>();
	public Vector<Range> els_ranges = new Vector<Range>();


	public Control(Main main) {
		this.p = main;

		b = p.controlP5.addTextfield("", x, y - 40, 150, 20);
		b.setAutoClear(false);

		p.controlP5.addButton("Trace", 0, x + 165, y - 40, 80, 19);
	}


	public void draw() {
		@SuppressWarnings("unchecked")
		Vector<TraceResult> traces_cp = (Vector<TraceResult>) p.traces.clone();

		int max = traces_cp.size() - 1;
		if (max > 10)
			max = 10;

		for (int a = max; a >= i; a--) {
			TraceResult ap = traces_cp.get(a);
			y += 40;

			float val = (140 / ap.getAveragePing()) % 255;
			els_labels.add(p.controlP5.addTextlabel("lbl" + y, ap.hostname, x,
					y));
			els_ranges.add(p.controlP5.addRange("" + ap.getAveragePing(), 0,
					255, 0, val, x, y + 15, 200, 12));

			i++;
		}

		// if (p.tracer.started && !p.tracer.finished) {
		if (i == 0 && p.tracer.started) {
			p.fill(p.WHITE);
			p.text("loading " + p.tracer.hostname + getLoadingDots(), x
					- p.shiftEarthX, yOriginal + 10);
		}
	}


	/**
	 * Reset all Control objects.
	 */
	public void reset() {
		y = yOriginal;
		i = 0;

		for (int a = 0; a < els_labels.size(); a++) {
			els_labels.get(a).remove();
			els_ranges.get(a).remove();
		}

		els_labels = new Vector<Textlabel>();
		els_ranges = new Vector<Range>();
	}

	/**
	 * What we do here is to make the "loading..." label dynamic by adding dots
	 * over time
	 */
	private int dots = 0;
	private int ellapsedFrames = 0;
	private int newDotEveryFrames = 30;


	private String getLoadingDots() {
		String s = "";

		if (ellapsedFrames >= newDotEveryFrames) {
			dots = ++dots % 4;
			ellapsedFrames = 0;
		}

		for (int i = 0; i <= dots; i++)
			s += ". ";

		ellapsedFrames++;
		return s;
	}
}
