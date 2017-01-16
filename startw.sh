# nohup /home/point/safen_cdr/startw.sh 로 호출해야 백그라운드로 구동됨
# 이것을 작업관리자에 등록하면 윈도우 서비스처럼 구동이 가능함.<단, 경로를 적절히 수정>
/home/point/safen_cdr/stop.sh >>"/home/point/safen_cdr/killme.txt"
ping 127.0.0.1 -c 4
ps -ef | grep safen_cdr/bin | grep java | grep -v grep | awk '{print $2}' | xargs kill -9
/home/java/bin/java -Dfile.encoding=UTF-8 -classpath ".:/home/point/safen_cdr/bin:/home/point/safen_cdr/libs/mysql-connector-java-5.1.35.jar:/home/point/safen_cdr/libs/log4j-1.2.17.jar" kr.co.cashq.safen_cdr.SAFEN_CDR /home/point/safen_cdr 1>/dev/null 2>&1 &
