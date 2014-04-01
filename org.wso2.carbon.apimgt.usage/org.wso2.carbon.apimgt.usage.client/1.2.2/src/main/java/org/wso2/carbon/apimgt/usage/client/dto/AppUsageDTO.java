package org.wso2.carbon.apimgt.usage.client.dto;

import java.util.Comparator;

/**
 * Created by asiri on 3/24/14.
 */
public class AppUsageDTO {

    private String appName;
    private String userid;
    private String consumerKey;
    private long count;


    public String getconsumerKey() {
        return consumerKey;
    }

    public void setconsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getappName() {
        return appName;
    }

    public void setappName(String appName) {
        this.appName = appName;
    }


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }


    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    //
    public static Comparator<AppUsageDTO> compareCount =
            new Comparator<AppUsageDTO>() {
                private AppUsageDTO ap1;
                private AppUsageDTO ap2;

                public int compare(AppUsageDTO ap1, AppUsageDTO ap2) {
                    this.ap1 = ap1;
                    this.ap2 = ap2;
                    return Long.compare(ap2.getCount(), ap1.getCount());
                }
            };
//
//    public long compare(AppUsageDTO ap1, AppUsageDTO ap2) {
//        return ap2.getCount() - ap1.getCount();
//    }

}
