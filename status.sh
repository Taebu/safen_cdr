ps -ef|grep 1182/bin|grep java|grep safen_cdr|awk 'BEGIN {FS=" "} {print $2}'

