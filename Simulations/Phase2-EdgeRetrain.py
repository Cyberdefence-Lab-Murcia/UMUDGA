from sklearn.model_selection import train_test_split
import os
import ntpath
from datetime import datetime
import numpy as np
from statistics import mean 
import statistics 
import re
import math
import random
import pickle
import scipy.stats as scstat            
from unidecode import unidecode
import datetime
from sklearn.ensemble import RandomForestClassifier
from datetime import timedelta
import numbers
from collections.abc import MutableMapping
import psutil
import timeit

import sklearn.model_selection as model_selection
import sklearn.metrics as metrics

# If true exports vectorial PDFs instead of JPG.
VECTORIAL_FIGURES = True
FIG_EXTENSION = "pdf" if VECTORIAL_FIGURES else "jpg"

ROOT_DIR = "/Experiments"
DATA_DIR = ROOT_DIR + "/"
EXP_DIR = DATA_DIR + "/"

# Change path to root
os.chdir(ROOT_DIR)
print(os.getcwd())


try:
    os.makedirs(EXP_DIR)
except FileExistsError:
    # directory already exists
    pass

import pandas as pd
pd.options.display.max_columns = None
pd.options.display.max_rows = None

def chunks(l, n):
    """Yield successive n-sized chunks from l."""
    for i in range(0, len(l), n):
        yield l[i:i + n]
        
from resource import getrusage as resource_usage, RUSAGE_SELF
from time import time as timestamp

def trace():
    print("Memory:", resource_usage(RUSAGE_SELF).ru_maxrss/1024, 
        "CPUTime:", str(resource_usage(RUSAGE_SELF).ru_utime))

proportion_of_original_shared_with_local = 0.20
proportion_of_zeroday_shared_with_cloud = 0.20
rf_base_estimators = 50
rf_edge_estimator_increase = 10

trace()
original_train_local_labels = None
with open(EXP_DIR + 'original_train_local_labels.pkl', 'rb') as handle:
    original_train_local_labels = pickle.load(handle)
original_train_local = None
with open(EXP_DIR + 'original_train_local.pkl', 'rb') as handle:
    original_train_local = pickle.load(handle)
original_test = None
with open(EXP_DIR + 'original_test.pkl', 'rb') as handle:
    original_test = pickle.load(handle)
original_test_labels = None
with open(EXP_DIR + 'original_test_labels.pkl', 'rb') as handle:
    original_test_labels = pickle.load(handle)
    
print("Data loaded")
trace()

print("Sharing Model and Data with Edge (" + "{:.0f}%".format(proportion_of_original_shared_with_local*100) + ")")
model = None
with open(EXP_DIR + 'model.pkl', 'rb') as handle:
    model = pickle.load(handle)
print("Model loaded")
trace()

ltrl = original_train_local_labels.unique()
ltrl.sort()
print("Labels known to edge: ", ltrl)
trace()

print("Edge adds " + str(rf_edge_estimator_increase) + " estimators...", end=" ")
model.n_estimators = model.n_estimators + rf_edge_estimator_increase
trace()

print("OK; Fitting (+" + str(original_train_local.shape[0]) + " samples)...", end=" ")
model.fit(original_train_local, original_train_local_labels)
print("OK;", end="\n")
trace()

print("Edge test with actual testing set")
original_pred_labels = model.predict(original_test)
trace()

print("- F1 Micro:", "{:.3f}".format(metrics.f1_score(original_test_labels, original_pred_labels, average="micro")), end="\n")
print("- F1 Macro:", "{:.3f}".format(metrics.f1_score(original_test_labels, original_pred_labels, average="macro")), end="\n")
trace()