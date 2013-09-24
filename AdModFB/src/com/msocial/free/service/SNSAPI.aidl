package com.msocial.free.service;

import oms.sns.TwitterTrends;

interface SNSAPI{             
        TwitterTrends getLast10Trends();
        String getSummary();     
          
}