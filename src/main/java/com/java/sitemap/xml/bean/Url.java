/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.sitemap.xml.bean;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Varghees Samraj
 */
public class Url {

    private String loc;
    private String changefreq;
    private Double priority;

    public String getLoc() {
        return loc;
    }

    @XmlElement
    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getChangefreq() {
        return changefreq;
    }

    @XmlElement
    public void setChangefreq(String changefreq) {
        this.changefreq = changefreq;
    }

    public Double getPriority() {
        return priority;
    }

    @XmlElement
    public void setPriority(Double priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Url{" + "loc=" + loc + ", changefreq=" + changefreq + ", priority=" + priority + '}';
    }
}
