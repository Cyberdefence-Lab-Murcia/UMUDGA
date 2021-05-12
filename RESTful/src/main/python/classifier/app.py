import datetime
import json
import math
import numbers
import os
import pickle
import random
import re
import statistics
from collections.abc import MutableMapping
from datetime import datetime, timedelta
from resource import RUSAGE_SELF
from resource import getrusage as resource_usage
from time import time as timestamp

import ntpath
import numpy as np
import pandas as pd
import psutil
import requests
import scipy.stats as scstat
import sklearn.metrics as metrics
import sklearn.model_selection as model_selection
from flask import Flask, jsonify, request
from scipy.io import arff
from unidecode import unidecode

app = Flask(__name__)

def init():
    #app.ROOT_DIR = "/home/mattia/DGA-RESTful/src/main/python/classifier/"
    app.ROOT_DIR = "/home/dga-fl-edge/DGA-RESTful/src/main/python/classifier/"
    #app.ROOT_DIR = "C:\\Users\\mattia\\Development\\DGA\\RESTful\\src\\main\\python\\classifier\\"
    app.DATA_DIR = app.ROOT_DIR + "Data/"
    app.MODELS_DIR = app.ROOT_DIR + "Models/"

    # Change path to root
    os.chdir(app.ROOT_DIR)

    try:
        os.makedirs(app.MODELS_DIR)
    except FileExistsError:
        # directory already exists
        pass

    app.features = pd.read_pickle(app.DATA_DIR + "dataframe_definition-000.pickle")
    app.featuresfs = pd.read_pickle(app.DATA_DIR + "dataframe_definition-10.pickle")

    with open(app.DATA_DIR + 'category_map_reversed.labels', 'rb') as f:
        app.category_map_reversed = pickle.load(f)
    with open(app.DATA_DIR + 'category_map.labels', 'rb') as f:
        app.category_map = pickle.load(f)

    with open(app.MODELS_DIR + 'lightgbm.scikit', 'rb') as f:
        app.lightgbm = pickle.load(f)
        print("Loaded model: ", app.lightgbm)

    with open(app.MODELS_DIR + 'lightgbm-FS10-Cl21-TrIn160000.scikit', 'rb') as f:
        app.lightgbmfs = pickle.load(f)
        print("Loaded model fs: ", app.lightgbmfs)

@app.route("/")
def home():
    return "Hello, Flask!"

@app.route("/domain/<fqdn>/features")
def getDomainFeatures(fqdn):
    return process(fqdn)

@app.route("/domain/<fqdn>/classify", methods=['GET','POST'])
def getDomainClassification(fqdn):

    if 'keep_features' in request.args:
        keep_features = True
    else:
        keep_features = False

    if request.method == 'POST':
        res = classify(fqdn, request.json, keep_features=keep_features)
    else:
        res =  classify(fqdn, keep_features=keep_features)
    return res

def process(domain, features_filter = None):
    headers = {'Content-Type': 'application/json'}
    times = {}
    times['features'] = {}
    try:
        t0_time, t0_resources = timestamp(), resource_usage(RUSAGE_SELF)
        data = {"fqdn": domain}
        endpoint = "http://155.54.210.169:8080/DGA/domain/features"
        if features_filter != None:
            data.update(features_filter)
            endpoint += "/filtered"

        r = requests.post(endpoint, data=json.dumps(data), headers=headers)
        t1_resources, t1_time = resource_usage(RUSAGE_SELF), timestamp()
        times['features']['request'] = {
            'wall': {'total': t1_time - t0_time},
            'user': {'total': t1_resources.ru_utime - t0_resources.ru_utime},
        }
        #print(r.json())
        features = {}
        for elem in r.json():
            for key in elem.keys():
                features[key] = elem[key]
                
        try:
            del(features['class'])
        except Exception:
            pass
        try:
            del(features['domain'])
        except Exception:
            pass

        t2_resources, t2_time = resource_usage(RUSAGE_SELF), timestamp()
        times['features']['cleaning'] = {
            'wall': {'total': t2_time - t1_time},
            'user': {'total': t2_resources.ru_utime - t1_resources.ru_utime},
        }
        times['features']['server'] = {
            'wall': {'total': float(r.headers['wall-time-ms'])/1000},
            'user': {'total': float(r.headers['cpu-time-ms'])/1000},
        }
        
        return {
            'domain': domain,
            'status_code': r.status_code,
            'features': features,
            'times': times
        }
    except Exception as ex:
        return {
            'domain': domain,
            'exception': ex
        }
    
def classify(domain, features_filter = None, keep_features = False):
    t0_time, t0_resources = timestamp(), resource_usage(RUSAGE_SELF)
    times = {}
    if features_filter == None:
        evaluation_features = pd.DataFrame(columns=app.features.columns)
    else:
        evaluation_features = pd.DataFrame(columns=app.featuresfs.columns)

    t1_resources, t1_time = resource_usage(RUSAGE_SELF), timestamp()
    times['setup'] = {
        'wall': {'total': t1_time - t0_time},
        'user': {'total': t1_resources.ru_utime - t0_resources.ru_utime},
    }
    
    res = process(domain, features_filter)
    
    res['times'].update(times)
    del(times)

    t1_resources, t1_time = resource_usage(RUSAGE_SELF), timestamp()
    evaluation_features = evaluation_features.append(res['features'], ignore_index=True).astype('float')    
    if not keep_features:
        del(res['features'])

    t2_resources, t2_time = resource_usage(RUSAGE_SELF), timestamp()
    res['times']['features']['postprocess'] = {
        'wall': {'total': t2_time - t1_time},
        'user': {'total': t2_resources.ru_utime - t1_resources.ru_utime},
    }
    
    res['class'] = {}
    if features_filter == None:
        res['class']['code'] = app.lightgbm.predict(evaluation_features)[0]
    else:
        res['class']['code'] = app.lightgbmfs.predict(evaluation_features)[0]
    t3_resources, t3_time = resource_usage(RUSAGE_SELF), timestamp()
    res['times']['classification'] = {
        'wall': {'total': t3_time - t2_time},
        'user': {'total': t3_resources.ru_utime - t2_resources.ru_utime},
    }
    
    res['class']['label'] = app.category_map[res['class']['code']]
    res['class']['code'] = int(res['class']['code'])
    
    t4_resources, t4_time = resource_usage(RUSAGE_SELF), timestamp()
    res['times']['total'] = {
        'wall': {'total': t4_time - t0_time},
        'user': {'total': t4_resources.ru_utime - t0_resources.ru_utime},
    }
    
    return res



init()
