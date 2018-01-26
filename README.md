
# Installation on Raspberry Pi

* Install Raspberry Pi operating system
```
    https://www.raspberrypi.org/documentation/installation/installing-images/
```

* Enable SSH
```
    https://www.raspberrypi.org/documentation/remote-access/ssh/
```

* Build
```
    mvn clean package
```

* Deploy
```
    scp exchange-stats-1.0.0.jar pi@192.168.1.1:~
    scp -r lib pi@192.168.1.1:~/lib
    scp example-exchange-stats.properties pi@192.168.1.1:~/exchange-stats.properties
```

* Run
```
    java -jar exchange-stats-1.0.0.jar
```