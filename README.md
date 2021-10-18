# Crouwdsourcing Application

## General description

The aim of this Android application is to scan the different Wi-Fi network
available and make a graph which show the number of available Wi-Fi
according to the time.

## Architecture

You have 3 main packages :

- data : This package permits to manipulate the storage of the data (GET,
ADD, DELETE)
- service : This package corresponds to the foreground service, this is the 
persistant service which will call the job which scan the Wi-Fi
- wifi : This package corresponds to the job which interact with the Wi-Fi as
start the scan and get the results

In the general package, you have also the MainActivity, which corresponds of
the display of the application.


