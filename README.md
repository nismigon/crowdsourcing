# Crouwdsourcing Application

## General description

The aim of this Android application is to scan the different Wi-Fi network
available and make a graph which show the number of available Wi-Fi
according to the time.

## Architecture

You have 3 main packages :

- data : This package permits to manipulate the storage of the data (GET,
ADD, DELETE)
- service : This package corresponds to the foreground service (the notification) and the Wi-Fi service

In the general package, you have also the MainActivity, which corresponds of
the display of the application with Google Maps, and you have the RawDataActivity
with the display of the raw data directly.


