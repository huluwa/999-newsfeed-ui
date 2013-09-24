package oms.sns.service.facebook.client;

import java.util.Map;

/**
 * Represents a bactched Facebook API request.
 * 
 * @author aroth
 */
//package-level access intentional (at least for now)
class BatchQuery {
    private FacebookMethod method;
    private Map<String, String> params;
    
    public BatchQuery(FacebookMethod method, Map<String, String> params) {
        this.method = method;
        this.params = params;
    }

    public FacebookMethod getMethod() {
        return method;
    }

    public void setMethod(FacebookMethod method) {
        this.method = method;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
