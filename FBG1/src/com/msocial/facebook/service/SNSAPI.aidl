package com.msocial.facebook.service;

import oms.sns.TwitterTrends;

interface SNSAPI{             
        TwitterTrends getLast10Trends();
        String getSummary();     
          
}