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
import nltk

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


for x in nltk.corpus.words.words():
    stopwords.add(x.translate(table,punc))

stopwords = set([stemm.stem_word(x) for x in stopwords])

#get a list of the normalized words from the tweet
def split_tweet(m):
    text = str(m['text']).translate(table, punc)    
    #repattern.sub('', 
    return set([x for x in [stemm.stem_word(x.lower()) for x in text.split() if x.lower().isalpha() and len(x) > 2 and  not '#' in x and  not 'http' in x] ])

#get hashtags
def get_tags(m):
    return [stemm.stem_word(x['text'].lower()) for x in m['entities']['hashtags']]

english_vocab = set(stemm.stem_word(w.lower()) for w in nltk.corpus.words.words()) 
print 'english parsed'
#text_vocab = set(w.lower() for w in text if w.lower().isalpha()) 
#loop over all tweets found in files on @path
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
                                m = json.loads(l)
                                tweet = split_tweet(m)
                                hashtags = get_tags(m)
                                unusual = tweet.difference(english_vocab) 
                                if len(unusual) > .3 * len(tweet):
                                    continue
                                function([t for t in tweet if not t in stopwords],hashtags,l,state)
                            except Exception, err:
                                #print err
                                pass 
                            
#find all hashtags in the corpus, called from loop_tweets
def get_hashtags(tweet,tags,l,hashtags):
    
    for tag in tags:
        if not hashtags.has_key(tag):
            hashtags[tag]=0
        hashtags[tag] =1+hashtags[tag]
                            
#filter out tweets that dont contain a hashtag found in tags, called from loop_tweets
def filter_hashtags(tweet,tags,l,output_tags):
    output = output_tags[0]
    hashtags = output_tags[1]
    b = False
    for tag in tags:
        if hashtags.has_key(tag):
            b = True
    if b:
        output.write('%s\n' % l)
#find counts of all words, called from loop_tweets
def term_count(tweet,tags,l,corpus_count):
    for w in tweet:
        if not corpus_count.has_key(w) :
            corpus_count[w] = 0
        corpus_count[w] = corpus_count[w]+1
        
#compute frequency of a word given a hashtag, called from loop_tweets
def class_count(tweet,tags,l,class_count_hashtags_term_count):
    class_count = class_count_hashtags_term_count[0]
    hashtags = class_count_hashtags_term_count[1]
    term_count =class_count_hashtags_term_count[2]
    tweetwords = tweet #split_tweet(l)
    for tag in [x for x in tags if hashtags.has_key(x) ]:
        for w in [x for x in tweetwords if term_count.has_key(x)]:
            if not class_count.has_key(tag):
                class_count[tag] = dict()
            if not class_count[tag].has_key(w):
                class_count[tag][w] = 0
            class_count[tag][w] = class_count[tag][w]+1

#write the csv used by weka to do classificaiton
#use only tweets with hashtags in @hashtags
#for each tweet write a line representing the feature vector of terms found 
#   in term_list
def write_classify_csv(tweet,tags,l,f_hashtags_term_dict):

    cfile = f_hashtags_term_dict[0]
    hashtags = f_hashtags_term_dict[1]
    term_list = f_hashtags_term_dict[2]
    num_csv = f_hashtags_term_dict[3]
    term_dict = dict()
    for x in term_list:
        term_dict[x] = 'F'
    tweetwords = tweet #split_tweet(l)
#    tags=get_tags(l)
    tag = tags[0]
    tagValue = 0
    b=False
    for t in tags:
        if  hashtags.has_key(t)  and hashtags[t] > tagValue:
            b = True
            tag = t
            tagValue = hashtags[t]
    
#    if not b :
#        num_csv[1]= num_csv[1]+ 1
#        return
    b = False

    for w in [x for x in tweetwords if term_dict.has_key(x)]:
        b = True
        term_dict[w] ='T' 

#    if not b :
        
#        num_csv[1]= num_csv[1]+ 1
#        return
    try:
        cfile.write("%s,%s\n" % (','.join([term_dict[x] for x in term_list]),tag))
        num_csv[0]= num_csv[0]+ 1
    except:
        print 'something terrible has happened'
    #f.write(','.join([term_dict[x] for x in term_list])+'\n')

#def hashtag_preprocess(l,f_hashtags_term_dict):
#    ret_dict = f_hashtags_term_dict[0]
#    hashtags = f_hashtags_term_dict[1]
#    term_list = f_hashtags_term_dict[2]
#    term_dict = dict()
#    for x in term_list:
#        term_dict[x] = 'F'
#    tweetwords = split_tweet(l)
#    tags=get_tags(l)
#    tag = tags[0]
#    tagValue = 0
#    b=False
#    for t in tags:
#        if  hashtags.has_key(t)  and hashtags[t] > tagValue:
#            b = True
#            tag = t
#            tagValue = hashtags[t]
#    
#    if not b :
#        return
#    b = False
#
#    for w in [x for x in tweetwords if term_dict.has_key(x)]:
#        ret_dict[tag] = True
#        return



#metric, takes a dictionary of cl_count[hashtag][word] = Count(word|hashtag)
def computeidf(terms,cl_count):
    idf = dict()
    for (term) in terms.keys():
        doccount = 1
        for (c,termdict) in cl_count.items():
            if termdict.has_key(term):
                doccount = doccount +1 
        idf[term] = math.log(float(len(cl_count))/(doccount))
    return idf

#metric, takes a dictionary of cl_count[hashtag][word] = Count(word|hashtag)
#     also takes the idf values for all distinct words
def compute_ktfidf_terms(k,class_count,idf):
    ret = set()
    for classname,termdict in class_count.items():
        
        topk = sorted([(term, termdict[term]*idf[term]) for term in termdict.keys()], key=itemgetter(1),reverse=True)

        for x,y in topk[0:k]:
            ret.add(x)
    return ret

#metric, takes a dictionary of cl_count[hashtag][word] = Count(word|hashtag)
def compute_ktf(k,class_count):
    ret = set()
    for classname,termdict in class_count.items():
        
        topk = sorted([(term, termdict[term]) for term in termdict.keys()], key=itemgetter(1),reverse=True)

        for x,y in topk[0:k]:
            ret.add(x)
    return ret




#metric, takes a dictionary of class_count[hashtag][word] = Count(word|hashtag)
#     also takes the idf values for all distinct words
#     also takes the terms_count values for all distinct words
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

#not used
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



#metric, takes a dictionary of class_count[hashtag][word] = Count(word|hashtag)
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
                if tpr ==0:
                    tpr = 0.0005

                if fpr ==0:
                    fpr = 0.0005
                mi_dict[term] = abs(norm.ppf(tpr)-norm.ppf(fpr))

        topk = sorted([(term, mi_dict[term]) for term in mi_dict.keys()], key=itemgetter(1),reverse=True)

        for x,y in topk[0:k]:
            ret.add(x)

    return ret

#metric, takes a dictionary of class_count[hashtag][word] = Count(word|hashtag)
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

#metric, takes a dictionary of class_count[hashtag][word] = Count(word|hashtag)
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





#metric, takes a dictionary of class_count[hashtag][word] = Count(word|hashtag)
#computes measures for the given hashtag-word combo
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

#loop over all tweets found here
twitterdir = '/media/My Passport/twitterdata/'
hashtags = dict()


if False:
    #get all hashtags and their counts
    loop_tweets(get_hashtags,hashtags,twitterdir)
    with open('tags_unfiltered.dump','w') as tagsfile:
        pickle.dump(hashtags,tagsfile)

else:
    with open('tags_unfiltered.dump') as tagsfile:
        hashtags = pickle.load(tagsfile)
print len(hashtags)
tags = sorted([(x,v) for (x,v) in hashtags.items()], key=itemgetter(1))
for x in [y for y in tags if len(y[0]) < 3]:
    del hashtags[x[0]]

tags = sorted([(x,v) for (x,v) in hashtags.items()], key=itemgetter(1))[:-40]
print '%d\n' % len(hashtags) 
for x in tags:
    del hashtags[x[0]]



tags = sorted([(x,v) for (x,v) in hashtags.items()], key=itemgetter(1))[-20:]
for x in tags:
    del hashtags[x[0]]

print hashtags
print "num hashtags %d" % len(hashtags)
filtered = '/home/phi/twitter/filtered/myout.json'
filteredPath = '/home/phi/twitter/filtered/'

if False:
#filter our dataset to only use tweets that have hashtags we are interested in
    with open(filtered,'w') as f:
        print "writing to %s" % filtered
        loop_tweets(filter_hashtags,[f,hashtags],twitterdir)

print 'getting ready to corpus count'    
corpus_count = dict()

if False:
    #get a count for each word found
    loop_tweets(term_count,corpus_count,filteredPath)
    print 'counted words in corpus'
    print "%d\n" % len(corpus_count)
    with open('corpus_count_unfiltered.dump','w') as tagsfile:
        pickle.dump(corpus_count,tagsfile)
else:
    with open('corpus_count_unfiltered.dump') as tagsfile:
        corpus_count=pickle.load(tagsfile)
#remove words that occur less than 200 times
c = sorted([(x,v) for (x,v) in corpus_count.items() if v < 5], key=itemgetter(1))
for x in c:
    del corpus_count[x[0]]


#c = sorted([(x,v) for (x,v) in corpus_count.items()] , key=itemgetter(1))[-15:]
#for x in c:
#    del corpus_count[x[0]]

print "corpus count %d" % len(corpus_count)
for x in stopwords:
    if corpus_count.has_key(x):
        del corpus_count[x]

print "corpus count %d" % len(corpus_count)



terms_set = set()
if True: 
    #for each hashtag compute Count(word|hashtag)
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

print "cl count %d" % len(cl_c)

#helper function to write out csv's for weka
def classify_io(folder,i,hashtags,terms_set):
    ret = [0,0]
    with open(folder + '%sterms.dump' % str(i),'w') as tagsfile:
        pickle.dump(terms_set,tagsfile)
       
    with open(folder + '%s.csv' % str(i),'w') as cfile:
        res_list = [x for x in terms_set]
        cfile.write(','.join(res_list)+',classifyhashtag\n')
        loop_tweets(write_classify_csv,[cfile,hashtags,res_list,ret],filteredPath)
    return ret
     

idf = computeidf(corpus_count,cl_c)
for i in range(1,40,5):     
    #    if True:
    try:
        print 'bns'
        
        terms_set = compute_bns(i,cl_c)
        print terms_set
        print classify_io("/home/phi/twitter/bns/",i,hashtags,terms_set) 

        print 'f1'
        terms_set = compute_f1(i,cl_c)
        print classify_io("/home/phi/twitter/f1/",i,hashtags,terms_set) 

        print 'acc2'
        terms_set = compute_acc2(i,cl_c)
        print classify_io("/home/phi/twitter/acc2/",i,hashtags,terms_set) 
     

        print 'ktfidf'
        terms_set= compute_ktfidf_terms(i,cl_c,idf)
        print classify_io("/home/phi/twitter/ktfidf/",i,hashtags,terms_set) 
    

        print 'ktf'
        terms_set = compute_ktf(i,cl_c)
        print classify_io("/home/phi/twitter/ktf/",i,hashtags,terms_set) 
            

        print 'global tfidf'
        terms_set = compute_global_tfidf(i*len(hashtags),cl_c,corpus_count,idf)
        print classify_io("/home/phi/twitter/global_tfidf/",i,hashtags,terms_set) 
    

#        print 'mut inf'
#        terms_set = compute_positive_mutual_information(i,cl_c,corpus_count,hashtags)
#        classify_io("/home/phi/twitter/pos_mut_info/",i,hashtags,terms_set) 
        
    except Exception, err:
        print str(err)

