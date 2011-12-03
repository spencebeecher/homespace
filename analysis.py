# -*- coding: utf-8 -*-
"""
Created on Sat Oct 29 12:50:19 2011

@author: -
"""

#!/usr/bin/env python
import numpy.numarray as na
import pickle
from operator import itemgetter, attrgetter
from pylab import *

import matplotlib.pyplot as plt
fig = matplotlib.pyplot.figure()
#tagspace

def plot_tags(hashtags):
    labels = ['' for x,y in tags]
    data =   [y for x,y in tags]
    
    xlocations = na.array(range(len(data)))+0.5
    width = 0.5
    bar(xlocations, data)#, width=width)
    #yticks(range(0, 8))
    #xticks(xlocations+ width/2, labels)
    xlim(0, xlocations[-1]+width*2)
    title("Frequency of Tags in Twitter Corpus")
    ax1 = fig.add_subplot(111)
    ax1.set_ylabel('Frequency')
    ax1.set_xlabel('Twitter Tags')
    gca().get_xaxis().tick_bottom()
    gca().get_yaxis().tick_left()
    
    show()

hashtags = []
filename = '/media/My Passport/tags_unfiltered.dump'
with open(filename) as tagsfile:
    hashtags = pickle.load(tagsfile)
    tags = sorted([(x,v) for (x,v) in hashtags.items() if v > 3500 ], key=itemgetter(1))
    print tags[-15:]


plot_tags(hashtags)
#
#matplotlib.pyplot.plot(f1[0],f1[1], label='f1', linewidth=2)
#matplotlib.pyplot.plot(acc2[0],acc2[1], label='acc2', linewidth=2)
#matplotlib.pyplot.plot(gtfidf[0],gtfidf[1], label='gtfidf', linewidth=2)
#matplotlib.pyplot.plot(bns[0],bns[1], label='bns', linewidth=2)
#matplotlib.pyplot.plot(tfidf[0],tfidf[1], label='tfidf', linewidth=2)
#matplotlib.pyplot.plot(tf[0],tf[1], label='tf', linewidth=2)
#matplotlib.pyplot.legend(('f1','acc2','gtfidf','bns','tfidf','tf'))
#matplotlib.pyplot.suptitle("Percent Correct of NB vs Feature Vector Size (20 Classes)", fontsize='20')
#
#unique tags
#unique words
#wordspace

#parameter space

#num tweets
