#!/bin/bash
set -e

WAR=http://epimorphics.com/maven2/com/epimorphics/data-api-app/0.0.1-SNAPSHOT/data-api-app-0.0.1-20140211.111111-3.war

apt-get update -y
apt-get install -y curl chkconfig

# install and configure tomcat
echo "** Installing java and tomcat"
apt-get install -y tomcat7 language-pack-en
service tomcat7 stop

# tomcat7 on ubuntu seems hardwired to java-6 so have to dance around this
# installing java-7 first doesn't work and we end up with failures in tomcat startup
apt-get install -y openjdk-7-jdk 
update-alternatives --set java /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java
unlink /usr/lib/jvm/default-java
ln -s /usr/lib/jvm/java-1.7.0-openjdk-amd64 /usr/lib/jvm/default-java

if [[ $(java -version 2>&1 | grep 1.7. -c) -ne 1 ]]
then
  echo "**   ERROR: java version doesn't look right, try manual alternatives setting restart tomcat7"
  echo "**   java version is:"
  java -version
  exit 1
fi
service tomcat7 start
chkconfig tomcat7 on

# install and configure nginx
echo "** Installing nginx"
apt-get install -y nginx
if [ $(grep -c nginx /etc/logrotate.conf) -ne 0 ]
then
  echo "**   logrotate for nginx already configured"
else
  cat /vagrant/install/nginx.logrotate.conf >> /etc/logrotate.conf
  echo "**   logrotate for nginx configured"
fi
cp /etc/nginx/sites-available/default  /etc/nginx/sites-available/original
cp /vagrant/install/nginx.conf /etc/nginx/sites-available/default

echo "**   starting nginx service ..."
service nginx restart
chkconfig nginx on

# Set up configuration area /opt/dsapi
echo "** installing the dsapi application"
if [ ! -d /opt/dsapi ]; then
  mkdir -p /opt/dsapi
fi
cp -R /vagrant/dsapi/* /opt/dsapi

rm -rf /var/lib/tomcat7/webapps/dsapi* || true
curl -4s --user $1 $WAR > /var/lib/tomcat7/webapps/dsapi.war
