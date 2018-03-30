Place Merger can run in any environment with Java installed that has access to a PostgreSQL database (whether locally or over a network).

Basic setup and run
===================
1. Unzip the latest Place Merger zip file
2. Create a new Postgres database at your PostgreSQL server (see instructions below for setting up Postgres locally), then run `sql/CreateTables.sql` on the database to set up Place Merger tables
3. Edit config.properties to include connection information for your database. "url" is any JDBC Postgres connection string (see https://jdbc.postgresql.org/documentation/80/connect.html for more information). Basically, it should be `jdbc:postgresql://[host]/database`. "user" and "password" are the user and pass to connect to the given database with. You will need to enable some sort of password-based authentication on your Postgres server for this to work.
4. Load populated place information into the database. Based on the format of each file you would like to insert into the database, run one of these commands:
    ```
    java -cp UDBInsert.jar UsgsToUDb [paths to USGS .csv files, separated by spaces]
    java -cp UDBInsert.jar NgaToUDb [paths to NGA tab-delimited .txt files, separated by spaces]
    java -cp UDBInsert.jar OSMAToUDb [paths to OpenStreetMap format A .csv files, separated by spaces]
    java -cp UDBInsert.jar OSMBToUDb [paths to OpenStreetMap format B .csv files, separated by spaces]
    ```
5. After you've finished loading data into the database, run the merge
    TODO merge

Local Database Setup
====================
If you would like to set up PostgreSQL for Place Merger locally, follow these instructions after you've unzipped Place Merger.
1. Do the basic install and setup of PostgreSQL on whatever OS you are running. You should be able to easily Google for instructions on setting up a basic Postgres install on whatever OS or flavor of Linux you are running. Keep track of what password you assign the 'postgres' database user as you set it up.
2. Once you have PostgreSQL running, run psql and enter these commands to create a new database and populate it with Place Merger tables:
    ```
    CREATE DATABASE placenames_udb ENCODING 'UTF8';
    \connect placenames_udb
    \i [path to CreateTables.sql]
    ```
3. Find where your pg_hba.conf file is on your platform. While running psql, enter this command:
    ```
    show hba_file;
    ```
4. Open up the pg_hba.conf file (you may need to do so with sudo/as root/as an administrator). Find the entries where the TYPE column is "local" or "host", and change each's METHOD to be "md5" (it will probably currently be "peer" or "ident"). This will allow Place Merger to connect with password authentication.
5. Set up config.properties as described above, using the URL "jdbc:postgresql://localhost/placenames_udb", the username "postgres", and the password whatever you set as the postgres password.
6. Run Place Merger as described above