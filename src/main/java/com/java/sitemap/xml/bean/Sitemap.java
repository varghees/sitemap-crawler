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
public class Sitemap {
    private String loc;
    private String lastmod;

    public String getLoc() {
        return loc;
    }

    @XmlElement
    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getLastmod() {
        return lastmod;
    }

    @XmlElement
    public void setLastmod(String lastmod) {
        this.lastmod = lastmod;
    }

    @Override
    public String toString() {
        return "Sitemap{" + "loc=" + loc + ", lastmod=" + lastmod + '}';
    }
}
