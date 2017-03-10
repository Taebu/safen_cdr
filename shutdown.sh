echo "=> Shutted Down Safen_cdr Agent..."
while :
do
pid=`/bin/ps -ef | grep safen_cdr/bin | grep java | grep -v grep | awk '{print $2}'`
if test "$pid" ; then
    kill -9  $pid
    echo $pid "is killed"
else
    echo "=> Shutted Down Safen_cdr Agent..."
    break
fi
done

