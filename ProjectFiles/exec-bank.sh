mvn clean install compile assembly:single
cp target/sdbank-1.0-SNAPSHOT-jar-with-dependencies.jar SDBank.jar
java -jar SDBank.jar