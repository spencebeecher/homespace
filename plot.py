# -*- coding: utf-8 -*-
"""
Created on Sat Oct 29 12:50:19 2011

@author: -
"""

#!/usr/bin/env python
import numpy.numarray as na

from pylab import *
tf = """30.85,10
52.75,69
57.72,114
60.51,159
61.78,196
63.64,238
64.3,261"""
    
tfidf = """30.79,17
51.63,82
58.07,146
61.75,199
62.89,231
64.02,267"""
        
bns = """32.1,14
48.28,53
50.72,82
52.9,117
54.69,140
56.95,163
58.77,182
59.38,196"""
            
gtfidf = """35.11,21
56.62,121
60.98,221"""
                
acc2 = """14.68,2
37.76,19
47.95,58
50.64,71
51.81,83
53.26,93
54.46,107"""
                    
f1 = """30.86,10
52.75,69
57.72,114
60.45,158
61.71,196
63.6,237"""

def get_series(series):
    tags= [x.split(',') for x in series.split('\n')]
    return [[x[1] for x in tags], [x[0] for x in tags]]

#tagspace
f1 = get_series(f1)
acc2= get_series(acc2)
gtfidf = get_series(gtfidf)
bns= get_series(bns)
tfidf= get_series(tfidf)
tf= get_series(tf)
    #error =  [0.3497             , 0.3108]
    
import matplotlib.pyplot as plt
fig = matplotlib.pyplot.figure()

matplotlib.pyplot.plot(f1[0],f1[1], label='f1', linewidth=2)
matplotlib.pyplot.plot(acc2[0],acc2[1], label='acc2', linewidth=2)
matplotlib.pyplot.plot(gtfidf[0],gtfidf[1], label='gtfidf', linewidth=2)
matplotlib.pyplot.plot(bns[0],bns[1], label='bns', linewidth=2)
matplotlib.pyplot.plot(tfidf[0],tfidf[1], label='tfidf', linewidth=2)
matplotlib.pyplot.plot(tf[0],tf[1], label='tf', linewidth=2)
matplotlib.pyplot.legend(('f1','acc2','gtfidf','bns','tfidf','tf'))
ax1 = fig.add_subplot(111)
ax1.set_ylabel('Percent Correct')
ax1.set_xlabel('Feature Vector Size')
matplotlib.pyplot.suptitle("Percent Correct of NB vs Feature Vector Size (20 Classes)", fontsize='20')
#    xlocations = na.array(range(len(data)))+0.5
#    width = 0.5
#    bar(xlocations, data)#, width=width)
#    yticks(range(0, 8))
#    xticks(xlocations+ width/2, labels)
#    xlim(0, xlocations[-1]+width*2)
#    title("Average Ratings on the Training Set")
#    gca().get_xaxis().tick_bottom()
#    gca().get_yaxis().tick_left()
    
show()
    
#unique tags
#unique words
#wordspace

#parameter space

#num tweets
