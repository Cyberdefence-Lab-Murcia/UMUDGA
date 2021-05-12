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
original_train_cloud_labels = None
with open(EXP_DIR + 'original_train_cloud_labels.pkl', 'rb') as handle:
    original_train_cloud_labels = pickle.load(handle)
original_train_cloud = None
with open(EXP_DIR + 'original_train_cloud.pkl', 'rb') as handle:
    original_train_cloud = pickle.load(handle)
original_test = None
with open(EXP_DIR + 'original_test.pkl', 'rb') as handle:
    original_test = pickle.load(handle)
original_test_labels = None
with open(EXP_DIR + 'original_test_labels.pkl', 'rb') as handle:
    original_test_labels = pickle.load(handle)

print("Data loaded")
trace()

ctrl = original_train_cloud_labels.unique()
ctrl.sort()
print("Labels known to cloud:", ctrl)

print("Create RF...", end=" ")
model = RandomForestClassifier(n_estimators = rf_base_estimators, random_state = 42, n_jobs=-1, warm_start=True)
print("OK;", model)
trace()

print("Fitting (" + str(original_train_cloud.shape[0]) + " samples)...", end=" ")
model.fit(original_train_cloud, original_train_cloud_labels)
print("OK;")
trace()

print("Cloud Cross-Evaluation")
print("- F1 Micro:", "{:.3f}".format(mean(model_selection.cross_val_score(model, original_train_cloud, original_train_cloud_labels, cv=3, scoring="f1_micro"))), end="\n")
print("- F1 Macro:", "{:.3f}".format(mean(model_selection.cross_val_score(model, original_train_cloud, original_train_cloud_labels, cv=3, scoring="f1_macro"))), end="\n")
trace()

print("Cloud test with actual testing set")
original_pred_labels = model.predict(original_test)
trace()

print("- F1 Micro:", "{:.3f}".format(metrics.f1_score(original_test_labels, original_pred_labels, average="micro")), end="\n")
print("- F1 Macro:", "{:.3f}".format(metrics.f1_score(original_test_labels, original_pred_labels, average="macro")), end="\n")
trace()

print("Dumping model to disk", end="")
with open(EXP_DIR + 'model.pkl', 'wb') as handle:
    pickle.dump(model, handle, protocol=pickle.HIGHEST_PROTOCOL)
print("; OK")
trace()