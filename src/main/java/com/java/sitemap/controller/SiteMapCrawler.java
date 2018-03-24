/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.sitemap.controller;

import com.java.sitemap.xml.bean.SiteBean;
import com.java.sitemap.service.SiteMapDownloaderService;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Varghees Samraj
 */
public class SiteMapCrawler {
    
    public static void main(String[] args) {
        String[] websites = {"https://www.ammaus.com", "http://www.nissanofcoolsprings.com"};
        Date start = new Date();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Set<Callable<SiteBean>> callables = new HashSet<Callable<SiteBean>>();
        for (String website : websites) {
            SiteMapDownloaderService sm = new SiteMapDownloaderService(website);
            callables.add(sm);
        }
        try {
            List<Future<SiteBean>> futures = executorService.invokeAll(callables);
        } catch (InterruptedException ex) {
            Logger.getLogger(SiteMapCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        Date end = new Date();
        System.out.println("Total Time ===> " + (end.getTime() - start.getTime()));
    }

}
