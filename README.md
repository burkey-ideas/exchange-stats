
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

*  Install Oracle Java 8
```
    sudo apt-get install oracle-java8-jdk -y
```

*  Change Timezone and Locale (Internationalization Options)
    *  https://www.raspberrypi.org/documentation/configuration/raspi-config.md
```
    sudo raspi-config
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

*  Obfuscate Passwords for Currency API Access Key and DNS Password
```
    java -cp exchange-stats-1.0.0.jar org.eclipse.jetty.util.security.Password [password]
```

*  Configure Properties File and set the Obfuscated Currency API Access Key and DNS Password
```
    vi exchange-stats.properties
```

*  Run
```
    java -jar exchange-stats-1.0.0.jar >> exchange-stats.log 2>&1 &
```

*  Shutdown
```
    ps -eaf | grep java
    kill -SIGTERM [PID]
    sudo shutdown -h now
```
