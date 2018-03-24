/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.sitemap.xml.bean;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Varghees Samraj
 */
@XmlRootElement(name = "sitemapindex")
public class SitemapIndex {

    private List<Sitemap> sitemap;

    public List<Sitemap> getSitemap() {
        return sitemap;
    }

    @XmlElement
    public void setSitemap(List<Sitemap> sitemap) {
        this.sitemap = sitemap;
    }

    @Override
    public String toString() {
        return "SitemapIndex{" + "sitemap=" + sitemap + '}';
    }
}
