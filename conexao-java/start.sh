#!/bin/bash
mvn clean package
LOG_SISTEMA="/home/ubuntu/gerenciamento-app-techguard/logSistema.sh"
LOG_NODE="/home/ubuntu/gerenciamento-app-techguard/logNode.sh"
LOG_JAVA="/home/ubuntu/gerenciamento-app-techguard/logJava.sh"
LOG_MYSQL="/home/ubuntu/gerenciamento-app-techguard/logMysql.sh"

echo -e "${YELLOW}Configurando CRON de Logs...${NC}"

CRON_SISTEMA="* * * * * bash $LOG_SISTEMA"
CRON_NODE="* * * * * bash $LOG_NODE"
CRON_JAVA="* * * * * bash $LOG_JAVA"
CRON_MYSQL="* * * * * bash $LOG_MYSQL"

(crontab -l | grep -Fxq "$CRON_SISTEMA") || (crontab -l; echo "$CRON_SISTEMA") | crontab -
(crontab -l | grep -Fxq "$CRON_NODE") || (crontab -l; echo "$CRON_NODE") | crontab -
(crontab -l | grep -Fxq "$CRON_JAVA") || (crontab -l; echo "$CRON_JAVA") | crontab -
(crontab -l | grep -Fxq "$CRON_MYSQL") || (crontab -l; echo "$CRON_MYSQL") | crontab -

echo '10 18 * * * ubuntu java -jar /usr/src/app/target/Integracao-1.0-SNAPSHOT-jar-with-dependencies.jar' > /etc/cron.d/mycron
chmod 755 /etc/cron.d/mycron
cat /etc/cron.d/mycron
crontab -l
chmod 755 target/Integracao-1.0-SNAPSHOT-jar-with-dependencies.jar
java -jar target/Integracao-1.0-SNAPSHOT-jar-with-dependencies.jar