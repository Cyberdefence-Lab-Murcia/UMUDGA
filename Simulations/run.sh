EXP=XXXXXX
mkdir $EXP
mprof run --interval 0.1 python3 Phase1-InitialTrain.py > $EXP/phase1.log 
mprof plot -o $EXP/1-InitialTrain.png --backend agg
mprof plot -o $EXP/1-InitialTrain.svg --backend agg
mprof run --interval 0.1 python3 Phase2-EdgeRetrain.py > $EXP/phase2.log 
mprof plot -o $EXP/2-EdgeRetrain.png --backend agg
mprof plot -o $EXP/2-EdgeRetrain.svg --backend agg
mprof run --interval 0.1 python3 Phase3-EdgeTestOnly.py > $EXP/phase3.log 
mprof plot -o $EXP/3-EdgeTestOnly.png --backend agg
mprof plot -o $EXP/3-EdgeTestOnly.svg --backend agg
