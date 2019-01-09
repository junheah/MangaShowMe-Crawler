package mangaview; // Main.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

// import org.jsoup.*;
// import org.jsoup.nodes.Document;
// import org.jsoup.nodes.Element;
// import org.jsoup.select.Elements;

//this is for testing
public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Main m = new Main();
		m.run();
	}
	public void run() {
		
		/*
		String view = "https://mangashow.me/bbs/board.php?bo_table=msm_manga&wr_id=";
		try {
			Document home = Jsoup.connect("https://mangashow.me/bbs/board.php?bo_table=msm_manga").get();
			System.out.println(home.text());
			Elements list = home.select("div.list-row");
			for(int i = 0; i<list.size(); i++) {
				System.out.println(i + ". " + list.get(i).selectFirst("div.subject").text());
			}
			int index = Integer.parseInt(getinput());
			String id = list.get(index).select("div.data-container").attr("data-wrid");
			System.out.println(id);
			//test
			Document manga = Jsoup.connect(view + id.toString()).get();
			Elements strip = manga.selectFirst("div.view-content.scroll-viewer").select("img");
			for(Element e:strip) {
				System.out.println(e.selectFirst("img").attr("src"));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		*/
		
		String query = getinput();
		Search a = new Search(query);
		ArrayList<Title> titles = a.getResult();
		int i=0;
		for(Title t:titles) {
			System.out.println(i+ ". " + t.getName()+"  |  " + t.getThumb());
			i++;
		}
		int index = Integer.parseInt(getinput());
		Title selected = titles.get(index);
		selected.fetchEps();
		ArrayList<Manga> mlist = selected.getEps();
		for(Manga m:mlist) {
			System.out.println(m.getId()+ ". " + m.getName());
		}
		
		index = Integer.parseInt(getinput());
		Manga man = mlist.get(index);
		man.fetch();
		for(String link:man.getImgs()) {
			System.out.println(link);
		}
		
		
	}
	
	public String getinput() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input="";
        try {
			input = br.readLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        return input;
	}

}
