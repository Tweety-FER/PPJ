#!/bin/bash

if [ -d krivi ]; then rm -rf krivi; fi;

correct=$((0))
dirs=$(find ./tests -type d | wc -l | tr -d ' ')
i=$((1))

for dir in $(find ./tests -type d); do
  java GLA < $dir/test.lan
  java analizator.LA < $dir/test.in > my.out
  diff --brief my.out $dir/test.out > /dev/null

  if [ $? -eq 1 ]; then
    if [ ! -d krivi ]; then mkdir krivi; fi;
    touch krivi/krivo_$i.out
    cat my.out >> krivi/krivo_$i.out
    echo -e "\n\nTocno je:\n" >> krivi/krivo_$i.out
    cat $dir/test.out >> krivi/krivo_$i.out
  else correct=$(($correct + 1))
  fi
  
  i=$(($i + 1))
done

rm -f my.out
echo "Tocno $correct/$dirs"
if [ -d krivi ]; then echo "Krivi u direktoriju krivi"; fi

