java -jar %DERBY_HOME%/lib/derbyrun.jar server start -p 9876

use -p to define a specific port

java -jar %DERBY_HOME%/lib/derbyrun.jar ij
connect 'jdbc:derby://localhost:9876/derbyDB';