package mangaview;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Title {
    public Title(String n, String t, String a, List<String> tg, int r) {
        name = n;
        thumb = t;
        author = a;
        tags = tg;
        release = r;
    }
    public String getName() {
        return name;
    }
    public String getThumb() {
        return thumb;
    }
    public List<Manga> getEps(){
        return eps;
    }
    public int getRelease() { return release; }

    public void fetchEps(String base) {
        //fetch episodes
        try {
            eps = new ArrayList<>();
            Document items = Jsoup.connect(base + "/bbs/page.php?hid=manga_detail&manga_name="+name)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                    .get();
            for(Element e:items.select("div.slot")) {
                eps.add(new Manga(Integer.parseInt(e.attr("data-wrid"))
                        ,e.selectFirst("div.title").ownText()
                        ,e.selectFirst("div.addedAt").ownText().split(" ")[0]));
            }
            thumb = items.selectFirst("div.manga-thumbnail").attr("style").split("\\(")[1].split("\\)")[0];
            author = items.selectFirst("a.author").ownText();
            tags = new ArrayList<>();
            for(Element e:items.selectFirst("div.manga-tags").select("a.tag")){
                tags.add(e.ownText());
            }
            try{
                String releaseRaw =  items.selectFirst("div.manga-thumbnail").selectFirst("a.publish_type").attr("href");
                release = Integer.parseInt(releaseRaw.substring(releaseRaw.lastIndexOf('=') + 1));
            }catch (Exception e){

            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    public String getAuthor(){
        if(author==null) return "";
        return author;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
    public int getBookmark(){ return bookmark;}
    public int getEpsCount(){ return eps.size();}
    public List<String> getTags(){
        if(tags==null) return new ArrayList<>();
        return tags;
    }
    public Boolean isNew() throws Exception{
        if(eps!=null){
            return eps.get(0).getName().split(" ")[0].contains("NEW");
        }else{
            throw new Exception("not loaded");
        }
    }

    public void setEps(List<Manga> list){
        eps = list;
    }

    public void removeEps(){
        if(eps!=null) eps.clear();
    }


    public void setBookmark(int b){bookmark = b;}

    private String name;
    private String thumb;
    private List<Manga> eps;
    private int bookmark=-1;
    String author;
    List<String> tags;
    int release;
}

