/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.sitemap.service;

import com.java.sitemap.xml.bean.Sitemap;
import com.java.sitemap.xml.bean.SitemapIndex;
import com.java.sitemap.xml.bean.Url;
import com.java.sitemap.xml.bean.Urlset;
import java.io.BufferedWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author Varghees Samraj
 */
public class SiteMapDownloaderService<T> implements Callable {

    public final String downloadSiteMapUrlPath = "/opt/sitemaps/";
    private String website;

    public SiteMapDownloaderService(String website) {
        this.website = website;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public Object call() throws Exception {
        downloadSiteMap();
        return website;
    }

    public List<Url> downloadSiteMap() {
        String urlPath = website.trim() + "/sitemap.xml";

        System.out.println("Downloading sitemap from " + website + " Sitemap: " + urlPath);
        String filename = downloadByProtocol(urlPath);
        if (filename == null) {
            if (website.trim().endsWith("/")) {
                urlPath = website.trim() + "sitemap.cfm";
            } else {
                urlPath = website.trim() + "/sitemap.cfm";
            }
        }
        filename = downloadByProtocol(urlPath);
        if (filename == null) {
            if (website.trim().endsWith("/")) {
                urlPath = website.trim() + "sitemap-inventory.cfm";
            } else {
                urlPath = website.trim() + "/sitemap-inventory.cfm";
            }
        }
        filename = downloadByProtocol(urlPath);
        if (filename == null) {
            if (website.trim().endsWith("/")) {
                urlPath = website.trim() + "resrc/xmlsitemap/sitemap-inventory-search/sitemap.xml";
            } else {
                urlPath = website.trim() + "/resrc/xmlsitemap/sitemap-inventory-search/sitemap.xml";
            }
        }
        filename = downloadByProtocol(urlPath);

        if (filename == null) {
            return null;
        }
        return getUrlsFromSiteMap(filename);
    }

    public String downloadByProtocol(String urlPath) {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            Set<Callable<String>> callables = new HashSet<Callable<String>>();
            callables.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String filename = downloadAndSave(getFileNameFromUrl(urlPath), urlPath);
                    if (filename == null) {
                        String protocol = "http://";
                        if (urlPath.startsWith("https")) {
                            protocol = "https://";
                        }
                        if (protocol.equalsIgnoreCase("http://")) {
                            urlPath.replaceFirst("http://", "https://");
                            filename = downloadAndSave(getFileNameFromUrl(urlPath), urlPath);
                        }
                        if (filename == null) {
                            return null;
                        }
                    }
                    return filename;
                }
            });
            List<Future<String>> futures = executorService.invokeAll(callables);
            for (Future<String> future : futures) {
                return future.get();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(SiteMapDownloaderService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(SiteMapDownloaderService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getDomainName(String url) {
        String protocol = "http://";
        if (url.contains("https")) {
            protocol = "https://";
        }
        url = url.replaceAll(protocol, "");
        String domainName = null;
        if (url.indexOf("/") > 0) {
            domainName = url.substring(0, url.indexOf("/"));
        } else {
            domainName = url.substring(0, url.length());
        }
        return domainName;
    }

    public String getFileNameFromUrl(String url) {
        String protocol = "http://";
        if (url.contains("https")) {
            protocol = "https://";
        }
        String domainName = getDomainName(url);
        String trimmedPath = url.replace(protocol, "");
        if (trimmedPath.isEmpty()) {
            trimmedPath = "default";
        }
        String filePath = downloadSiteMapUrlPath + domainName + "/" + trimmedPath;
        if (filePath.endsWith("/")) {
            filePath += "default";
        }
        if (filePath.endsWith(domainName)) {
            filePath += "/default";
        }
        return filePath;
    }

    private String downloadAndSave(String fileName, String urlStr) {
        String savedFile = fileName;
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

// Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        if (urlStr != null) {
            InputStream input = null;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                byte[] buffer = new byte[8 * 1024];
                int status = conn.getResponseCode();
                boolean redirected = false;
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER) {
                        redirected = true;
                        String newUrl = conn.getHeaderField("Location");
                        conn = (HttpURLConnection) new URL(newUrl).openConnection();
                    }
                }
                System.out.println("url: " + urlStr + " Response Code: " + conn.getResponseCode() + " Response Message: " + conn.getResponseMessage());
                if (conn.getResponseCode() < 300) {
                    input = conn.getInputStream();
                    if (fileName.endsWith("gz") || fileName.endsWith("GZ")) {
                        savedFile = savedFile.replaceAll(".gz", "");
                        savedFile = savedFile.replaceAll(".GZ", "");
                        uncompress(input, savedFile);
                    } else {
                        try {
                            File file = new File(fileName);
                            boolean mkdirs = file.getParentFile().mkdirs();
                            OutputStream output = null;
                            try {
                                output = new FileOutputStream(fileName);
                            } catch (FileNotFoundException ex) {
                                removeIfFileExist(file.getParentFile());
                                mkdirs = file.getParentFile().mkdirs();
                                try {
                                    output = new FileOutputStream(fileName);
                                } catch (FileNotFoundException ex1) {
                                    ex1.printStackTrace();
                                }
                            }
                            int bytesRead;
                            while ((bytesRead = input.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }
                            output.close();
                        } finally {
                            if (input != null) {
                                input.close();
                            }
                        }
                    }
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return savedFile;
    }

    private void removeIfFileExist(File parentFile) {
        if (parentFile == null) {
            return;
        }
        if (parentFile.isDirectory()) {
            return;
        }
        if (parentFile.exists() && !parentFile.isDirectory()) {
            parentFile.delete();
        } else if (!parentFile.exists()) {
            removeIfFileExist(parentFile.getParentFile());
        }
    }

    private void uncompress(InputStream input, String savedFile) {
        byte[] buffer = new byte[1024];
        GZIPInputStream gzis = null;
        try {
            gzis = new GZIPInputStream(input);

            FileOutputStream out
                    = new FileOutputStream(savedFile);
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            gzis.close();
            out.close();

            System.out.println("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<Url> getUrlsFromSiteMap(String filename) {
        SitemapIndex sitemap = (SitemapIndex) com.java.sitemap.utils.FileReader.readXML(filename, SitemapIndex.class);
        List<Url> urls = new ArrayList<>();
        if (sitemap != null) {
            for (Iterator<Sitemap> iterator = sitemap.getSitemap().iterator(); iterator.hasNext();) {
                Sitemap sitemapData = iterator.next();
                website = sitemapData.getLoc();
                List<Url> urlsFromSiteMap = downloadSiteMap();
                if (urlsFromSiteMap != null && !urlsFromSiteMap.isEmpty()) {
                    urls.addAll(urlsFromSiteMap);
                }
            }
        } else {
            Urlset urlSet = (Urlset) com.java.sitemap.utils.FileReader.readXML(filename, Urlset.class);
            if (urlSet != null) {
                urls = urlSet.getUrl();
            } else {
                try {
                    Document doc = Jsoup.parse(new File(filename), "utf-8");
                    Elements tables = doc.select("table#sitemap");
                    if (tables != null) {
                        Element siteMapTable = tables.first();
                        if (siteMapTable != null) {
                            for (Element urlLink : siteMapTable.select("a")) {
                                Url tdUrl = new Url();
                                tdUrl.setLoc(urlLink.attr("href"));
                                urls.add(tdUrl);
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SiteMapDownloaderService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        String downloadContentPath = downloadSiteMapUrlPath + getDomainName(website) + "/" + "contentUrl";
        downloadContentUrl(urls, downloadContentPath);
        System.out.println("Content Url File Path === > " + downloadContentPath);
        return urls;
    }

    public Boolean isSiteMap(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith("xml") || url.endsWith("gz") || lowerUrl.contains("sitemap")) {
            return true;
        }
        return false;
    }

    public void downloadContentUrl(List<Url> urls, String downloadContentPath) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Set<Callable<String>> callables = new HashSet<Callable<String>>();
        for (Iterator<Url> iterator = urls.iterator(); iterator.hasNext();) {
            Url url = iterator.next();
            callables.add(new Callable<String>() {
                @Override
                public String call() {
                    String fileName = downloadByProtocol(url.getLoc());
                    return fileName;
                }
            });
        }
        try {
            executorService.invokeAll(callables);
        } catch (InterruptedException ex) {
            Logger.getLogger(SiteMapDownloaderService.class.getName()).log(Level.SEVERE, null, ex);
        }
        File file = new File(downloadContentPath);
        removeIfFileExist(file);
        if (urls != null) {
            FileWriter fw = null;
            BufferedWriter bw = null;
            try {
                fw = new FileWriter(downloadContentPath, true);
                bw = new BufferedWriter(fw);
                for (Iterator<Url> iterator = urls.iterator(); iterator.hasNext();) {
                    Url url = iterator.next();
                    bw.write(url.getLoc());
                    bw.newLine();
                }
            } catch (IOException ex) {
                Logger.getLogger(SiteMapDownloaderService.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    if (fw != null) {
                        fw.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SiteMapDownloaderService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
