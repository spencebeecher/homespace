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

#tagspace
hashtags = []
with open('tags_unfiltered.dump') as tagsfile:
    hashtags = pickle.load(tagsfile)
    tags = sorted([(x,v) for (x,v) in hashtags.items() if v > 3500 ], key=itemgetter(1))
    print tags[-1]


def plot_tags(hashtags):
    labels = [x for x,y in tags]
    data =   [y for x,y in tags]
    #error =  [0.3497             , 0.3108]
    
    xlocations = na.array(range(len(data)))+0.5
    width = 0.5
    bar(xlocations, data)#, width=width)
    yticks(range(0, 8))
    xticks(xlocations+ width/2, labels)
    xlim(0, xlocations[-1]+width*2)
    title("Average Ratings on the Training Set")
    gca().get_xaxis().tick_bottom()
    gca().get_yaxis().tick_left()
    
    show()

plot_tags(hashtags)
    
#unique tags
#unique words
#wordspace

#parameter space

#num tweets
