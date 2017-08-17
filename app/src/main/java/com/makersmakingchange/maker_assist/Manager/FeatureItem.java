package com.makersmakingchange.maker_assist.Manager;

/**
 * Created by Milad on 5/15/2017.
 */

public class FeatureItem {

    String featureName;
    int featureFont;
    //int featureImage;


    public FeatureItem(String featureName, int featureFont)
    {
        //this.featureImage=featureImage;
        this.featureName=featureName;
        this.featureFont=featureFont;
    }
    public String getFeatureName()
    {
        return featureName;
    }
    public int getFeatureFont()
    {
        return featureFont;
    }

}