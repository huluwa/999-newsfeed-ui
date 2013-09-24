package com.ast.free.service;

import oms.sns.TwitterTrends;

interface SNSAPI{             
        TwitterTrends getLast10Trends();
        String getSummary();     
          
}