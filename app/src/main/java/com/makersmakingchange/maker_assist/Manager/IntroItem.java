package com.makersmakingchange.maker_assist.Manager;

/**
 * Created by Milad on 5/15/2017.
 */

public class IntroItem {

    int introTitle;
    int introDescription;
    int introNextButtonText;

    public IntroItem(int introTitle, int introDescription,int introNextButtonText)
    {

        this.introTitle=introTitle;
        this.introDescription=introDescription;
        this.introNextButtonText=introNextButtonText;
    }
    public int getIntroTitle()
    {
        return introTitle;
    }
    public int getIntroDescription()
    {
        return introDescription;
    }
    public int getIntroNextButtonText()
    {
        return introNextButtonText;
    }
}