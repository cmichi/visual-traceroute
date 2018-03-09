# Readme

This is a graphical frontend for the Unix `traceroute` utility. It displays the geographic locations to which a URL is resolved. 

It has been written using the Processing API. These librariers were also used: GLGraphics, toxiclibs and controlP5.

Requirements are:

 * `traceroute`
 * `dig`
 * Java

**Project Status:** I have written this application five years ago and haven't done much work on it since. The application should still work under a Unix system (at the time I tested it under Linux and Mac OS X) with a decent Java version. 


# Eye Candy

![Current status](https://github.com/cmichi/visual-traceroute/raw/master/images/shot.png)

I also uploaded a short video to [Vimeo](http://vimeo.com/26674070).


# Known Issues

Some bugs left and I want to resolve the `dig` dependency.
Known issue: Loading bar vanishes after first station.


# How to start the application?

You can either import everything as an Eclipse project, adjust the Build path and do `Run As > Java Applet` or you can use the standalone version:

	tar -xf standalone.tar.gz 
	cd standalone/
	ln -s ../geoip geoip
	java -Djava.library.path=./library -jar trace.jar


# Maps

The textures are taken from the [NASA's Blue Marble project](https://visibleearth.nasa.gov/view.php?id=57723). 


# GeoIP

The GeoIP lookup is done using a GeoIP database:

	Under the license agreement, all advertising materials and documentation 
	mentioning features or use of this database must display the following 
	acknowledgment: "This product includes GeoLite data created by MaxMind, 
	available from http://www.maxmind.com/."

You can download the current GeoIP database [here](http://geolite.maxmind.com/download/geoip/database/).


# Contributors

 * Sébastien Saunier provided a fix for a missing file issue


# License

	Copyright (c) 
		
		2011 Michael Mueller, http://micha.elmueller.net/
	
	Permission is hereby granted, free of charge, to any person obtaining
	a copy of this software and associated documentation files (the
	"Software"), to deal in the Software without restriction, including
	without limitation the rights to use, copy, modify, merge, publish,
	distribute, sublicense, and/or sell copies of the Software, and to
	permit persons to whom the Software is furnished to do so, subject to
	the following conditions:

	The above copyright notice and this permission notice shall be
	included in all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
	NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
	LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
	OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
	WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
