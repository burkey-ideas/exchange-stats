
# Installation on Raspberry Pi

*  Install Raspberry Pi operating system
    *  https://www.raspberrypi.org/documentation/installation/installing-images/
```
    diskutil list
    diskutil unmountDisk /dev/disk3
    sudo dd bs=1m if=2018-06-27-raspbian-stretch-lite.img of=/dev/rdisk3 conv=sync
    
    sudo diskutil eject /dev/rdisk3
```

*  Enable SSH
    *  https://www.raspberrypi.org/documentation/remote-access/ssh/
```
    touch /Volumes/boot/ssh
```

*  Enable WiFi (optional)
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

*  Static IP (optional)
    *  https://www.raspberrypi.org/learning/networking-lessons/rpi-static-ip-address/
    *  https://wiki.archlinux.org/index.php/dhcpcd
```
    sudo vi /etc/dhcpcd.conf
    
    interface eth0
    static ip_address=192.168.1.100/24
    static routers=192.168.1.1
    static domain_name_servers=192.168.1.1
```

*  Update and Upgrade
    *  https://www.raspberrypi.org/documentation/raspbian/updating.md
```
    sudo apt-get update
    sudo apt-get dist-upgrade
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
    scp target/exchange-stats-1.0.0.jar pi@192.168.1.100:~/exchange-stats.jar
    scp -r target/lib pi@192.168.1.100:~/lib
    scp example-exchange-stats.properties pi@192.168.1.100:~/exchange-stats.properties
    scp src/main/scripts/exchange-stats.sh pi@192.168.1.100:~
```
    
```
    sudo mv ~/exchange-stats.sh /etc/init.d/exchange-stats.sh
    sudo chmod 755 /etc/init.d/exchange-stats.sh
```

*  Obfuscate Passwords for Currency API Access Key and Dynamic DNS Password
```
    java -cp exchange-stats.jar org.eclipse.jetty.util.security.Password [password]
```

*  Configure Properties File
```
    vi exchange-stats.properties
```

*  Run (either)
```
    java -jar exchange-stats.jar
    sudo /etc/init.d/exchange-stats.sh start
```

*  Register for Start Up
```
    sudo update-rc.d exchange-stats.sh defaults
```

*  Shutdown
```
    sudo /etc/init.d/exchange-stats.sh stop
    sudo shutdown -h now
```
