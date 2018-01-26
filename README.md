
# Installation on Raspberry Pi

*  Install Raspberry Pi operating system
    *  https://www.raspberrypi.org/documentation/installation/installing-images/
```
    diskutil list
    diskutil unmountDisk /dev/disk2
    sudo dd bs=1m if=2017-11-29-raspbian-stretch-lite.img of=/dev/rdisk2 conv=sync
```

*  Enable SSH
    *  https://www.raspberrypi.org/documentation/remote-access/ssh/
```
    touch /Volumes/boot/ssh
```

*  Enable WiFi
    *  https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md
```
    sudo iwlist wlan0 scan
    wpa_passphrase "ssid-name" "password"
    sudo vi /etc/wpa_supplicant/wpa_supplicant.conf
    
    network={
      ssid="ssid-name"
      psk=xxx
    }
    
    wpa_cli -i wlan0 reconfigure
```

*  Install Java
```
    sudo apt-get install oracle-java8-jdk -y
```

*  Build
```
    mvn clean package
```

*  Deploy
```
    scp exchange-stats-1.0.0.jar pi@192.168.1.1:~
    scp -r lib pi@192.168.1.1:~/lib
    scp example-exchange-stats.properties pi@192.168.1.1:~/exchange-stats.properties
```

*  Run
```
    java -jar exchange-stats-1.0.0.jar > exchange-stats.log 2>&1 &
```
