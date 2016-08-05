ps -ef|grep safen_cdr/bin|grep java|grep safen_cdr|awk 'BEGIN {FS=" "} {print $2}'

