package com.zhj.ESDemo.utils;

import com.zhj.ESDemo.pojo.Content;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Topic
 * Description
 *
 * @author zhouh
 * @version 1.0
 * Create by 2022/7/24 20:47
 */
public class HtmlParseUtil {
    public static void main(String[] args) throws IOException {
        new HtmlParseUtil().parseJD("中文").forEach(System.out::println);
        //等价于for增强
      /*  for (User user : users) {
            System.out.println(user);
        }*/
    }

    public List<Content> parseJD(String keyword) throws IOException{
        //获取请求
        //前提：需要联网，ajax不能获取到
        String url = "https://search.jd.com/Search?keyword=" + keyword;
        //解析网页  ,(jsoup 返回的对象就是浏览器document对象)
        Document document = Jsoup.parse(new URL(url), 30000);
        //所有你在js中可以使用的方法，在这里都可以用。
        Element j_goodsList = document.getElementById("J_goodsList");
        //  System.out.println(j_goodsList.html());
        //获取所有的li元素
        Elements li = j_goodsList.getElementsByTag("li");
        //list
        ArrayList<Content> objects = new ArrayList<>();
        //获取元素中的内容
        for (Element element : li) {
            // System.out.println("element = " + element);
           /* Elements img = element.getElementsByTag("img").eq(0);
            System.out.println("img = " + img);*/
            Content content = new Content();
            //关于这种图片很多网站，图片都是懒加载的
            String img_src = element.getElementsByTag("img").eq(0).attr("data-lazy-img");
            //System.out.println("img_src = " + img_src);
            String price = element.getElementsByClass("p-price").eq(0).text();
            //System.out.println("price = " + price);
            String title = element.getElementsByClass("p-name").eq(0).text();
            //System.out.println("title = " + title);
            content.setImg(img_src);
            content.setPrice(price);
            content.setTitle(title);
            objects.add(content);
        }
        return objects;
    }
}
