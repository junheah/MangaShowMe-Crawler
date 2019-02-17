package mangaview;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.text.DecimalFormat;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Main m = new Main();
		m.run();
	}
	String baseDir = "C:/Users/junhea/Desktop/test/";
	String base = "https://mangashow2.me";
	public void run() {
		while(true) {
			System.out.print("검색어: ");
			String input = getInput();
			Search s = new Search(input, 0);
			ArrayList<Title> result;
			while(true) {
				s.fetch(base);
				result = s.getResult();
				if(!s.isLast()) System.out.println("다음 페이지 : n");
				else System.out.println("마지막 페이지 입니다.");
				for(int i=0; i<result.size(); i++) {
					Title t = result.get(i);
					System.out.println(i+". "+t.getName()+" | "+t.getAuthor());
				}
				System.out.print(">> ");
				input = getInput();
				if(!input.matches("n")) break;
			}
			downloadTitle(result.get(Integer.parseInt(input)));
		}
	}
	
	public void downloadTitle(Title t) {
		t.fetchEps(base);
		String name = t.getName();
		File dir = new File(baseDir+name);
		if(!dir.exists()) dir.mkdir();
		List<Manga> mangas = t.getEps();
		for(int i=mangas.size()-1; i>=0; i--) {
			Manga m = mangas.get(i);
			System.out.println("downloading: "+m.getName());
			downloadManga(m);
		}
	}
	
	public int getIndex(ArrayList<Manga> eps, int id){
        for(int i=0; i<eps.size(); i++){
            if(eps.get(i).getId()==id){
                return eps.size()-i;
            }
        }
        return 0;
    }
	
	public void downloadManga(Manga m) {
		m.fetch(base);
		List<String> urls = m.getImgs();
		int id = m.getId();
		int seed = m.getSeed();
		String name = m.getTitle().getName();
		String ep = m.getName();
		String targetDir = baseDir+name+'/'+new DecimalFormat("000").format(getIndex(m.getEps(),id))+". "+ep+'/';
		File dir = new File(targetDir);
		if(!dir.exists()) dir.mkdir();
		Decoder d = new Decoder(seed, id);
		for (int i = 0; i < urls.size(); i++) {
			System.out.println((i+1)+"/"+urls.size());
            try {
                URL url = new URL(urls.get(i));
                if(url.getProtocol().toLowerCase().matches("https")) {
                    HttpsURLConnection init = (HttpsURLConnection) url.openConnection();
                    int responseCode = init.getResponseCode();
                    if (responseCode >= 300) {
                        url = new URL(init.getHeaderField("location"));
                    }
                }else{
                    HttpURLConnection init = (HttpURLConnection) url.openConnection();
                    int responseCode = init.getResponseCode();
                    if (responseCode >= 300) {
                        url = new URL(init.getHeaderField("location"));
                    }
                }
                String fileType = url.toString().substring(url.toString().lastIndexOf('.') + 1);
                URLConnection connection = url.openConnection();

                /*
                BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                File outputFile = new File(targetDir + (new DecimalFormat("0000").format(i)));
                FileOutputStream out = new FileOutputStream(outputFile.toString() + '.'+fileType);
                byte[] data = new byte[1024];
                int length = connection.getContentLength();
                int count;
                while ((count = in.read(data, 0, 1024)) != -1) {
                    out.write(data, 0, count);
                }
                */

                //load image as bitmap
                //InputStream in = connection.getInputStream();
                BufferedImage image = ImageIO.read(connection.getInputStream());
                //Bitmap bitmap = BitmapFactory.decodeStream(in);
                //decode image
                image = d.decode(image);
                //save image
                File outputFile = new File(targetDir + (new DecimalFormat("0000").format(i)) + ".jpg");
                ImageIO.write(image, "jpg", outputFile);

            } catch (Exception e) {
                //
                e.printStackTrace();
            }
        }
        //in case imgStepSize grounds to zero
    }
	
	public String getInput() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input="";
        try {
			input = br.readLine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return input;
	}

}
