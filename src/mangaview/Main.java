package mangaview;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//this is for testing
public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Main m = new Main();
		m.run();
	}
	public void run() {
		System.out.print("제목 검색:");
		String query = getinput();
		Search a = new Search(query, 0);
		a.fetch();
		ArrayList<Title> titles = a.getResult();
		int i=0;
		for(Title t:titles) {
			System.out.println(i+ ". " + t.getName()+"  |  " + t.getThumb());
			i++;
		}
		System.out.print("만화 선택:");
		int index = Integer.parseInt(getinput());
		Title selected = titles.get(index);
		selected.fetchEps();
		ArrayList<Manga> mlist = selected.getEps();
		for(Manga m:mlist) {
			System.out.println(m.getId()+ ". " + m.getName());
		}
		System.out.print("화 선택:");
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
