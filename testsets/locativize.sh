#!/bin/bash

while IFS='' read -r line || [[ -n "$line" ]]; do
    fem="echo \"$line|subst:sg:loc:f\" | fsa_morph -P -d ./fsa_morph/polish_synth.dict"
    m1="echo \"$line|subst:sg:loc:m1\" | fsa_morph -P -d ./fsa_morph/polish_synth.dict"
    m2="echo \"$line|subst:sg:loc:m2\" | fsa_morph -P -d ./fsa_morph/polish_synth.dict"
    m3="echo \"$line|subst:sg:loc:m3\" | fsa_morph -P -d ./fsa_morph/polish_synth.dict"
    n1="echo \"$line|subst:sg:loc:n1\" | fsa_morph -P -d ./fsa_morph/polish_synth.dict"
    n2="echo \"$line|subst:sg:loc:n2\" | fsa_morph -P -d ./fsa_morph/polish_synth.dict"
    declare -a arr=("$fem" "$m1" "$m2" "$m3" "$n1" "$n2") 
    for i in "${arr[@]}"
    do
        inflected=$(eval $i)
        if [[ ! "$inflected" =~ "not found" ]]; then
            echo $inflected | sed 's/^.*: //g'
        fi
    done
done < "$1"
