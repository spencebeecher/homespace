# -*- coding: utf-8 -*-
"""
Spyder Editor

This temporary script file is located here:
/home/phi/.spyder2/.temp.py
"""
import json
from scipy.stats import *
import os
import pickle
from operator import itemgetter, attrgetter
import string, sys, re
import math
from nltk.stem.porter import PorterStemmer

stemm = PorterStemmer()
table = string.maketrans("","")
punc = string.punctuation.replace('#','')


repattern = re.compile('[\W_]+')

    
stopwords = set("""a
about
above
after
again
against
all
am
an
and
any
are
aren't
as
at
be
because
been
before
being
below
between
both
but
by
can't
cannot
could
couldn't
did
didn't
do
does
doesn't
doing
don't
down
during
each
few
for
from
further
had
hadn't
has
hasn't
have
haven't
having
he
he'd
he'll
he's
her
here
here's
hers
herself
him
himself
his
how
how's
i
i'd
i'll
i'm
i've
if
in
into
is
isn't
it
it's
its
itself
let's
me
more
most
mustn't
my
myself
no
nor
not
of
off
on
once
only
or
other
ought
our
ours
ourselves
out
over
own
same
shan't
she
she'd
she'll
she's
should
shouldn't
so
some
such
than
that
that's
the
their
theirs
them
themselves
then
there
there's
these
they
they'd
they'll
they're
they've
this
those
through
to
too
under
until
up
very
was
wasn't
we
we'd
we'll
we're
we've
were
weren't
what
what's
when
when's
where
where's
which
while
who
who's
whom
why
why's
with
won't
would
wouldn't
you
you'd
you'll
you're
you've
your
yours
yourself
yourselves""".translate(table,punc).split())


def split_tweet(l):
    if type(l) == list:
        return l[0]
    m = json.loads(l)
    text = str(m['text']).translate(table, punc)    
    #repattern.sub('', 
    return set([stemm.stem_word(x.lower()) for x in text.split() if not '#' in x and not x in stopwords and not 'http' in x])


def get_tags(l):
    if type(l) == list:
        return l[1]
    m = json.loads(l)
    return [x['text'].lower() for x in m['entities']['hashtags']]


def loop_tweets(function,state,path='./'):   
    print "loop tweets %s\n" % path
    for dirname, dirnames, filenames in os.walk(path):
        if dirname == path:
            for filename in filenames:
                if 'json' in filename :
                   
                    with open(path+filename) as f:
                        for l in f.xreadlines():
                            try:
                                l = l.strip()
                                function(l,state)
                            except Exception, err:
                                #print err
                                pass 
                            
         
def loop_tweets_mem(mem,function,state):   
    for l in mem:
        function(l,state)

def get_memory_tweets(l,tweet_list):
    tweet_list.append([split_tweet(l),get_tags(l)])

def get_hashtags(l,hashtags):
    
    for tag in get_tags(l):
        if not hashtags.has_key(tag):
            hashtags[tag]=0
        hashtags[tag] =1+hashtags[tag]
                            

def filter_hashtags(l,output_tags):
    output = output_tags[0]
    hashtags = output_tags[1]
    b = False
    for tag in get_tags(l):
        if hashtags.has_key(tag):
            b = True
    if b:
        output.write('%s\n' % l)

def term_count(l,corpus_count):
    for w in split_tweet(l):
        if not corpus_count.has_key(w) :
            corpus_count[w] = 0
        corpus_count[w] = corpus_count[w]+1
        
#class,word,count
def class_count(l,class_count_hashtags_term_count):
    class_count = class_count_hashtags_term_count[0]
    hashtags = class_count_hashtags_term_count[1]
    term_count =class_count_hashtags_term_count[2]
    tweetwords = split_tweet(l)
    for tag in [x for x in get_tags(l) if hashtags.has_key(x) ]:
        for w in [x for x in tweetwords if term_count.has_key(x)]:
            if not class_count.has_key(tag):
                class_count[tag] = dict()
            if not class_count[tag].has_key(w):
                class_count[tag][w] = 0
            class_count[tag][w] = class_count[tag][w]+1


def write_classify_csv(l,f_hashtags_term_dict):
    cfile = f_hashtags_term_dict[0]
    hashtags = f_hashtags_term_dict[1]
    term_list = f_hashtags_term_dict[2]
    term_dict = dict()
    for x in term_list:
        term_dict[x] = 'F'
    tweetwords = split_tweet(l)
    tags=get_tags(l)
    tag = tags[0]
    tagValue = 0
    b=False
    for t in tags:
        if  hashtags.has_key(t)  and hashtags[t] > tagValue:
            b = True
            tag = t
            tagValue = hashtags[t]
    
    if not b :
        return
    b = False

    for w in [x for x in tweetwords if term_dict.has_key(x)]:
        b = True
        term_dict[w] ='T' 

#    if not b :
#        return
    try:
        cfile.write("%s,%s\n" % (','.join([term_dict[x] for x in term_list]),tag))
    except:
        print 'something terrible has happened'
    #f.write(','.join([term_dict[x] for x in term_list])+'\n')




def computeidf(terms,cl_count):
    idf = dict()
    for (term) in terms.keys():
        doccount = 1
        for (c,termdict) in cl_count.items():
            if termdict.has_key(term):
                doccount = doccount +1 
        idf[term] = math.log(float(len(cl_count))/(doccount))
    return idf

#for each class get the tfidf values for each term in the class
#   return the set of combined terms 
def compute_ktfidf_terms(k,class_count,idf):
    ret = set()
    for classname,termdict in class_count.items():
        
        topk = sorted([(term, termdict[term]*idf[term]) for term in termdict.keys()], key=itemgetter(1),reverse=True)

        for x,y in topk[0:k]:
            ret.add(x)
    return ret

def compute_ktf(k,class_count):
    ret = set()
    for classname,termdict in class_count.items():
        
        topk = sorted([(term, termdict[term]) for term in termdict.keys()], key=itemgetter(1),reverse=True)

        for x,y in topk[0:k]:
            ret.add(x)
    return ret



def compute_global_tfidf(k,class_count,terms_count,idf):
    retdict = dict()
    ret = set()
    for term in terms_count.keys():
        for classname,termdict in class_count.items():
            if termdict.has_key(term):
                if retdict.has_key(term) :
                    retdict[term] = retdict[term] + termdict[term]
                else:
                    retdict[term] = termdict[term]

    topk = sorted([(term, retdict[term]*idf[term]) for term in retdict.keys()], key=itemgetter(1),reverse=True)

    for x,y in topk[0:k]:
        ret.add(x)
    return ret


def compute_positive_mutual_information(k,class_count,terms_count,hashtags):
    ret = set()
    for classname,termdict in class_count.items():
        p_c = sum([y for x,y in termdict.items()]) 
        mi_dict = dict()
        for term,p_t in terms_count.items():
            if termdict.has_key(term):
                p_c_t = termdict[term]  
                div = hashtags[classname]
                mi_dict[term] = p_c_t/div*math.log((p_c_t + 1.0)/div /((1.0+ p_c)/div *(1.0+p_t)/div))

        topk = sorted([(term, mi_dict[term]) for term in mi_dict.keys()], key=itemgetter(1),reverse=True)

        for x,y in topk[0:k]:
            ret.add(x)
    return ret

def compute_bns(k,class_count):
    ret = set()
    for classname,termdict in class_count.items():
        mi_dict = dict()
        for term,p_t in termdict.items():
                rates = compute_tp_fp_tn_fn(classname,term,class_count)
                tp = rates[0]
                fp = rates[1]
                tn = rates[2]
                fn = rates[3]
                tpr = tp/(tp+fn)
                fpr = fp / (fp + tn)
                mi_dict[term] = abs(norm.ppf(tpr)-norm.ppf(fpr))

        topk = sorted([(term, mi_dict[term]) for term in mi_dict.keys()], key=itemgetter(1),reverse=True)

        for x,y in topk[0:k]:
            ret.add(x)

    return ret

def compute_acc2(k,class_count):
    ret = set()
    for classname,termdict in class_count.items():
        mi_dict = dict()
        for term,p_t in termdict.items():
                rates = compute_tp_fp_tn_fn(classname,term,class_count)
                tp = rates[0]
                fp = rates[1]
                tn = rates[2]
                fn = rates[3]
                tpr = tp/(tp+fn)
                fpr = fp / (fp + tn)
                mi_dict[term] = abs(tpr-fpr)

        topk = sorted([(term, mi_dict[term]) for term in mi_dict.keys()], key=itemgetter(1),reverse=True)

        for x,y in topk[0:k]:
            ret.add(x)

    return ret

def compute_f1(k,class_count):
    ret = set()
    for classname,termdict in class_count.items():
        mi_dict = dict()
        for term,p_t in termdict.items():
                rates = compute_tp_fp_tn_fn(classname,term,class_count)
                tp = rates[0]
                fp = rates[1]
                tn = rates[2]
                fn = rates[3]
                tpr = tp/(tp+fn)
                fpr = fp / (fp + tn)
                mi_dict[term] = 2* tp /(tp + fn +tp +fp)
        topk = sorted([(term, mi_dict[term]) for term in mi_dict.keys()], key=itemgetter(1),reverse=True)

        for x,y in topk[0:k]:
            ret.add(x)

    return ret





def compute_tp_fp_tn_fn(tag,word,class_count):
    #tp number of cases containing the word
    #fp number of negative cases containing the word
    #tn number of cases not containing the word
    #fn number of negative cases not containing the word
    tp = 0.0 #class_count[tag][word]
    fp = 0.0 #class_count[*-tag][word]
    tn = 0.0 #class_count[tag][*-word]
    fn = 0.0 #class_count[*-tag][*-word]

    for classname,termdict in class_count.items():
        for term,p_t in termdict.items():
            if classname == tag:
                if term == word:
                    tp = tp + p_t
                else:
                    tn = tn + p_t
            else:
                if term == word:
                    fp = fp + p_t
                else:
                    fn = fn + p_t

    return [tp,fp,tn,fn]


twitterdir = '/media/My Passport/twitterdata/'
hashtags = dict()

if False:
    loop_tweets(get_hashtags,hashtags,twitterdir)
    with open('tags_unfiltered.dump','w') as tagsfile:
        pickle.dump(hashtags,tagsfile)

else:
    with open('tags_unfiltered.dump') as tagsfile:
        hashtags = pickle.load(tagsfile)

tags = sorted([(x,v) for (x,v) in hashtags.items()], key=itemgetter(1))[:-100]
print '%d\n' % len(hashtags) 

for x in tags:
    del hashtags[x[0]]


tags = sorted([(x,v) for (x,v) in hashtags.items()], key=itemgetter(1))[-60:]
for x in tags:
    del hashtags[x[0]]

print "num hashtags %d" % len(hashtags)
filtered = '/home/phi/twitter/filtered/myout.json'
filteredPath = '/home/phi/twitter/filtered/'

if True:
    with open(filtered,'w') as f:
        print "writing to %s" % filtered
        loop_tweets(filter_hashtags,[f,hashtags],twitterdir)

print 'getting ready to corpus count'    
corpus_count = dict()

if True:
    loop_tweets(term_count,corpus_count,filteredPath)
    print 'counted words in corpus'
    print "%d\n" % len(corpus_count)
    with open('corpus_count_unfiltered.dump','w') as tagsfile:
        pickle.dump(corpus_count,tagsfile)
else:
    with open('corpus_count_unfiltered.dump') as tagsfile:
        corpus_count=pickle.load(tagsfile)

c = sorted([(x,v) for (x,v) in corpus_count.items() if v < 100], key=itemgetter(1))
for x in c:
    del corpus_count[x[0]]
terms_set = set()
if True: 
    cl_c = dict()
    loop_tweets(class_count,[cl_c,hashtags,corpus_count],filteredPath)
    print 'looped tweets'
    print len(cl_c)
    print len(corpus_count) 
    
    with open('cl_count.dump','w') as tagsfile:
        pickle.dump(cl_c,tagsfile)    
       
else:

    with open('cl_count.dump') as tagsfile:
        cl_c = pickle.load(tagsfile)


def classify_io(folder,i,hashtags,terms_set):
    with open(folder + '%sterms.dump' % str(i),'w') as tagsfile:
        pickle.dump(terms_set,tagsfile)
       
    with open(folder + '%s.csv' % str(i),'w') as cfile:
        res_list = [x for x in terms_set]
        cfile.write(','.join(res_list)+',hashtag\n')
        loop_tweets(write_classify_csv,[cfile,hashtags,res_list],filteredPath)
     

idf = computeidf(corpus_count,cl_c)
for i in range(1,40,5):     
    #    if True:
    try:
        print 'bns'
        terms_set = compute_bns(i,cl_c)
        classify_io("/home/phi/twitter/bns/",i,hashtags,terms_set) 

        print 'f1'
        terms_set = compute_f1(i,cl_c)
        classify_io("/home/phi/twitter/f1/",i,hashtags,terms_set) 

        print 'acc2'
        terms_set = compute_acc2(i,cl_c)
        classify_io("/home/phi/twitter/acc2/",i,hashtags,terms_set) 
     
        print 'ktfidf'
        terms_set= compute_ktfidf_terms(i,cl_c,idf)
        classify_io("/home/phi/twitter/ktfidf/",i,hashtags,terms_set) 
    
        print 'ktf'
        terms_set = compute_ktf(i,cl_c)
        classify_io("/home/phi/twitter/ktf/",i,hashtags,terms_set) 
            
        print 'global tfidf'
        terms_set = compute_global_tfidf(i*len(hashtags),cl_c,corpus_count,idf)
        classify_io("/home/phi/twitter/global_tfidf/",i,hashtags,terms_set) 
    
#        print 'mut inf'
#        terms_set = compute_positive_mutual_information(i,cl_c,corpus_count,hashtags)
#        classify_io("/home/phi/twitter/pos_mut_info/",i,hashtags,terms_set) 
        
    except Exception, err:
        print str(err)

