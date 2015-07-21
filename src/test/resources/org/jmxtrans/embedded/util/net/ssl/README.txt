
keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 3600 -keysize 2048

keytool -genkeypair -keystore keystore.jks -validity 3600 -dname "CN=jmxtrans.org, OU=jmxtrans, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" -keypass password -storepass password -keyalg RSA -alias selfsigned -ext SAN=dns:www.jmxtrans.org,ip:127.0.0.1

keytool -export -keystore keystore.jks -alias selfsigned -file selfsigned.cer

keytool -import -file selfsigned.cer -alias selfsigned -keystore truststore.jks
