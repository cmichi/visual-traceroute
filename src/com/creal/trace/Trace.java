package com.creal.trace;

import com.*;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creal.geo.Connection;
import com.creal.geo.Coordinate;
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

/**
 * This Thread reads & processes the output of the traceroute utility.
 * 
 * @author cmichi
 */
public class Trace extends Thread {
	private Main p;

	public String hostname;
	public boolean started = false;


	public Trace(Main main) {
		this.p = main;
	}


	/**
	 * Read the output from the traceroute utitlity and add it to the Collection
	 * Main.traces
	 */
	public void run() {
		started = true;
		try {
			String line;
			Process p = Runtime.getRuntime().exec(
					this.p.configFile.getProperty("TRACEROUTE_PATH") + " "
							+ hostname);
			BufferedReader input = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			while ((line = input.readLine()) != null) {
				TraceResult tr = this.parse(line.trim());
				// System.out.println(line + ", " + tr);

				if (tr != null) {
					this.p.traces.add(tr);

					if (tr.geolocation != null
							&& this.p.connections.size() >= 1) {
						Connection tr_prev = this.p.connections
								.get(this.p.connections.size() - 1);
						if (tr_prev.co2.equals(tr.geolocation) == false)
							addNewConnection(
									this.p.connections.get(this.p.connections
											.size() - 1).co2, tr.geolocation);
					}

					if (tr.geolocation != null
							&& this.p.connections.size() == 0) {
						Iterator<TraceResult> tr_ = this.p.traces.iterator();
						while (tr_.hasNext()) {
							TraceResult tr__ = tr_.next();
							if (tr__.geolocation != null
									&& tr__.geolocation.equals(tr.geolocation) == false)
								addNewConnection(tr__.geolocation,
										tr.geolocation);
						}
					}

				}

			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}


	/**
	 * Parse a line of the traceroute output.
	 * 
	 * @param line
	 * @return
	 */
	private TraceResult parse(String line) {
		TraceResult tr = new TraceResult();
		line = line.trim();
		String[] es = line.split(" ");

		/* a processLine might contain just a sequence of "* * *" */
		Pattern processingLinePattern = Pattern.compile("[*]\\s");
		Matcher processingLine = processingLinePattern.matcher(line);

		Pattern correctLinePattern = Pattern.compile("^[\\d]*\\s.*$");
		Matcher correctLine = correctLinePattern.matcher(line);

		/* is it even possible to parse? */
		if (processingLine.find() == false && correctLine.matches() == true) {
			tr.hostname = es[2];
			tr.ip = es[3].replace("(", "").replace(")", "");
			tr.i = new Integer(es[0]);

			/*
			 * first entry is my local ip, since we often are behind a router in
			 * a LAN we want the public ip address to be used here (and not
			 * 192.168.0.2 ..).
			 */
			if (tr.i == 1)
				tr.ip = getMyIP();

			tr.pingTime = new ArrayList<String>();
			tr.pingTime.add(es[5]);

			if (es.length >= 9)
				tr.pingTime.add(es[8]);
			if (es.length >= 12)
				tr.pingTime.add(es[11]);

			tr.geolocation = lookupIP(tr.ip);

			return tr;
		}

		return null;
	}


	private void addNewConnection(Coordinate co1, Coordinate co2) {
		Connection con = new Connection(p, co1, co2);
		con.currShiny = p.currShiny;

		con.conn2 = new Connection(p, co1, co2);
		con.conn2.currShiny = con.currShiny;

		con.conn2.conn2 = new Connection(p, co1, co2);
		con.conn2.conn2.currShiny = con.currShiny;
		p.currShiny = ++p.currShiny % p.SHINY.length;
		
		p.connections.add(con);
	}


	/**
	 * Returns the ip of me exposed to the internet. So the WAN-IP is returned,
	 * not the LAN-IP!
	 * 
	 * @return
	 */
	private String getMyIP() {
		Process p;
		try {
			p = Runtime
					.getRuntime()
					.exec(this.p.configFile.getProperty("DIG_PATH")
							+ " +short myip.opendns.com @resolver1.opendns.com");
			BufferedReader input = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			return input.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}


	/**
	 * Return Coordinate for a given IP.
	 * 
	 * @param ip
	 * @return
	 */
	private Coordinate lookupIP(String ip) {
		try {
			LookupService ls = new LookupService(
					this.p.configFile.getProperty("GEOIP_CITY_DATABASE"),
					LookupService.GEOIP_MEMORY_CACHE);
			Location loc = ls.getLocation(ip);
			ls.close();

			String co = loc.latitude + " " + loc.longitude;
			return new Coordinate(this.p, co);
		} catch (Exception e) {
			System.out.println(e);
		}

		return null;
	}

}
