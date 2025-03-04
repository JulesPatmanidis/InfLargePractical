# Food delivery drone controller

This project was part of the Informatics Large Practical course (2022) at the University of Edinburgh. The project implements a drone controller algorithm aiming to optimize the path of a delivery drone given the orders received, the pickup/delivery locations and designated "no-fly-zones".

## Project Structure

This project consist of 3 main parts:
- The Drone controller (Java Application)
  - Fetches order,menu and geo data from the DB and the webserver.
  - Calculates an optimal drone path for a given date taking into account pickup/delivery locations, drone battery, order price and "no-fly-zones".
  - Stores "completed" orders and flightpath information on the DB.
- The Orders Database (DerbyDB)
  - Stores information about pending and completed orders for all dates.
- The Data Webserver (Java Application)
  - Stores Information about store locations, menus and "no-fly-zones".

## Deployment

First, deploy the database:

```bash
java -jar %DERBY_HOME%/lib/derbyrun.jar server start -p 9876
```

Then, deploy the web server:
```bash
java -jar website/WebServerLite.jar ∼/InfLargePractical/website 9898
```

Then run the Java application:

```bash
java -jar target/ilp-1.0-SNAPSHOT.jar <DD> <MM> <YYYY>
```

The output is a geojson that can be viewed in any online GEOJSON viewer. No-fly-zones and pickup locations can be overlayed by using the all.geojson file the testing folder. 