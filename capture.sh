#!/bin/bash 
i=0
while true; do
   i=$((i + 1))
    echo The counter is $i
        curl https://stream.twitter.com/1/statuses/sample.json -uuser:pass> twitter$i.json
done

