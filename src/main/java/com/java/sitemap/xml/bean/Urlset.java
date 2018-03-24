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
@XmlRootElement(name = "urlset")
public class Urlset {
    private List<Url> url;

    public List<Url> getUrl() {
        return url;
    }

    @XmlElement
    public void setUrl(List<Url> url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Urlset{" + "url=" + url + '}';
    }
    
}
