# Place-Merger overview

## Data flow
![data flow diagram][dataFlow]

## Unified Database
### ERD
![ERD][erd]
### Purpose
The Unified Database (UDb) is the key step between raw data in files and merged output.
After the various ...ToUdb files are run, it will contain the information necessary to perform merging.
This means that the data in the UDb has many duplicates and many missing parts. 
The key advantages of the UDb are easy access to data and a consistent format.

## ...ToUDb
Each of these files 
1. Reads data in a given format into the Unified Database
2. Inherits from SourceToUDb.java which is an abstract class
3. Can be run by itself
4. Are not terribly well-refined due to time constraints

### Geojson format
There is a GeojsonToUDb.java file, but it doesn't work. 
Json is just really annoying to parse, and the polygons are hard to use.
I think the polygons could be very powerful and useful, but they take a lot of space to represent.
There is currently a problem where it eats your entire heap, and we didn't think it was worth fixing.


## Backlog
There are several things which could be useful to add to the UDb including
- NGA feature codes
- NGA Generic 
- State, county, etc
- geojson centroids
- ODM_b other tags language codes
- geojson polygons


[erd]: https://github.com/LightSys/Place-Merger/raw/doc/docs/Place-Merger-ERD.png
[dataFlow]: https://github.com/LightSys/Place-Merger/raw/doc/docs/Place-Merger-Data-Flow.png
