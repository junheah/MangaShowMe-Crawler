package mangaview;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Main {

	CustomHttpClient c;
	Preference p;
	SpinnerThread spinner;
	static final char[] spin = { '|', '/', '-', '\\' };
	static final DecimalFormat dformat = new DecimalFormat("0000");

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImageIO.scanForPlugins();
		int argc = args.length;
		if (argc == 0) {
			Main m = new Main();
			m.run();
		} else if (argc == 1) {
			if (args[0].equals("help")) {
				help();
				return;
			}
			// url input
			Main m = new Main();
			m.init();
			m.downloadUrl(args[0]);
		}
	}

	public static void help() {
		System.out.println("Usage: ");
		System.out.println("no args\t: 프로그램 실행");
		System.out.println("h\t: 이 정보 표시");
		System.out.println("<URL>\t: 입력된 URL의 내용 다운로드");
	}

	public void stopSpin() {
		spinner.pause();
		System.out.print("     \r");
	}

	public void startSpin() {
		System.out.println();
		spinner.again();
	}

	public void run() {
		welcome();
		init();
		int option = 1;
		while (option > 0) {
			menu();
			option = getInt();

			if (option == 1) {
				if (p.getUrl().length() == 0)
					setUrl();
				search();
			} else if (option == 2) {
				downloadUrl();
			} else if (option == 3) {
				commentBot();
			} else if (option == 4) {

			} else if (option == 5) {

			}
		}
		stopSpin();
		spinner.interrupt();
	}

	public void downloadUrl() {
		System.out.print("URL 입력: ");
		String input = getInput();
		downloadUrl(input);
	}

	public void downloadUrl(String input) {
		// get base url
		if (p == null) {
			p = new Preference();
		}
		String[] surl = input.split("/");
		String protocol = surl[0];
		String home = surl[2];
		p.setUrl(protocol + "//" + home + '/');

		if (input.contains("/bbs/board.php") && input.contains("bo_table=manga") && input.contains("wr_id")) {
			// manga
			Manga m = new Manga(input);
			downloadManga(null, m);

		} else if (input.contains("/bbs/page.php") && input.contains("hid=manga_detail")
				&& input.contains("manga_id")) {
			// title
			Title target = new Title(input);
			List<Manga> download = selectEps(target);
			if (download.size() > 0) {
				System.out.println(download.size() + " 개 다운로드 시작.");
				target.setEps(download);
				downloadTitle(target);
			}

		}
	}

	public void welcome() {
		System.out.println("\nManaMoa Crawler 마나모아 크롤러 v.0.0.1\n");
	}

	public void setUrl() {
		System.out.print("사이트 url을 입력해 주세요 (https://xxxxxxxxx.net/) 형식\n>> ");
		p.setUrl(getInput());
	}

	public void init() {
		spinner = new SpinnerThread();
		spinner.start();
		startSpin();
		p = new Preference();
		c = new CustomHttpClient(p);
		stopSpin();
	}

	public void menu() {
		System.out.print("Menu:\n1. 직접 검색해서 다운로드\n2. URL로 다운로드\n3. 최신 만화 댓글 봇\n>> ");
	}

	public void search() {
		String query = "";
		while (query.length() == 0) {
			System.out.print("제목 검색: ");
			query = getInput();
		}
		Search search = new Search(query, 0);
		List<Title> result = new ArrayList<>();
		int index = 0;
		Title target = null;
		int input = -1;

		while (target == null) {
			if (!search.isLast() && input == -1) {
				startSpin();
				search.fetch(c);
				stopSpin();
				result.addAll(search.getResult());

				for (; index < result.size(); index++) {
					Title t = result.get(index);
					System.out.println(index + ". " + t.getName() + " / " + t.getAuthor());
				}
			}
			System.out.print("선택 (-1: 다음 페이지 로드, -2: 취소) : ");
			input = getInt();
			if (input > -1 && input < result.size()) {
				target = result.get(input);
			} else if (input == -2) {
				return;
			}
		}

		// title selected

		List<Manga> download = selectEps(target);
		if (download.size() > 0) {
			System.out.println(download.size() + " 개 다운로드 시작.");
			target.setEps(download);
			downloadTitle(target);
		}
	}

	public List<Manga> selectEps(Title target) {
		startSpin();
		target.fetchEps(c);
		stopSpin();
		System.out.print("\n**만화 정보**\n제목: " + target.getName() + "\n작가: " + target.getAuthor() + "\n만화 ID: "
				+ target.getId() + "\n썸네일: " + target.getThumb() + "\n태그: ");
		for (String t : target.getTags()) {
			System.out.print(t + ' ');
		}
		List<Manga> episodes = target.getEps();

		System.out.print("\n총 " + episodes.size() + " 화\n");

		for (int i = 0; i < episodes.size(); i++) {
			Manga m = episodes.get(i);
			System.out.println(i + ". " + m.getName() + " / " + m.getDate() + " / " + m.getId());
		}

		List<Manga> download = new ArrayList<>();

		while (download.size() == 0) {
			System.out.println(
					"\n\t** 입력 예시 **\n\t1\t: 1 선택\n\t1-10\t: 1부터 10까지 선택\n\t1,5,4-8\t: 1, 5, 4부터 8까지 선택\n\tall\t: 전체 선택\n");
			System.out.print("선택 (-1: 취소)>> ");
			String selection = getInput();

			if (selection.equals("-1"))
				return download;

			try {
				if (selection.toLowerCase().contains("all")) {
					download.addAll(episodes);
				} else {
					// split with ,
					if (selection.contains(",")) {
						String[] ss = selection.split(",");
						for (String sss : ss) {
							if (sss.contains("-")) {
								String[] ssss = sss.split("-");
								int i1 = Integer.parseInt(ssss[0]);
								int i2 = Integer.parseInt(ssss[1]);
								for (; i1 <= i2; i1++) {
									download.add(episodes.get(i1));
								}
							} else {
								int i0 = Integer.parseInt(sss);
								download.add(episodes.get(i0));
							}
						}
					} else {
						if (selection.contains("-")) {
							String[] ssss = selection.split("-");
							int i1 = Integer.parseInt(ssss[0]);
							int i2 = Integer.parseInt(ssss[1]);
							for (; i1 <= i2; i1++) {
								download.add(episodes.get(i1));
							}
						} else {
							int i0 = Integer.parseInt(selection);
							download.add(episodes.get(i0));
						}
					}
				}
			} catch (Exception e) {
				System.out.println("입력 포맷 오류. 입력값을 확인해 주세요.");
			}

		}
		return download;
	}

	public void downloadTitle(Title t) {
		List<Manga> mangas = t.getEps();
		File dir = new File(filterFolder(t.getName()));
		if (!dir.exists())
			dir.mkdir();
		for (int i = mangas.size() - 1; i >= 0; i--) {
			Manga m = mangas.get(i);
			System.out.println("다운로드 시작: " + m.getName());
			downloadManga(dir, m);
		}

	}

	public void downloadManga(File tdir, Manga m) {
		startSpin();
		m.fetch(c);
		stopSpin();

		List<String> urls = m.getImgs();
		int id = m.getId();
		int seed = m.getSeed();
		// String encodedName =
		File dir = null;
		if (tdir == null)
			dir = new File(filterFolder(dformat.format(m.getRealIndex()) + "." + m.getName()));
		else
			dir = new File(tdir, filterFolder(dformat.format(m.getRealIndex()) + "." + m.getName()));

		if (!dir.exists())
			dir.mkdirs();

		System.out.println(dir.getAbsolutePath());

		Decoder d = new Decoder(seed, id);

		boolean hasSecond = m.getImgs(true).size() > 0;

		int counter = 0;

		// download images
		int prevBufferSize = 0;
		for (int i = 0; i < urls.size(); i++) {
			boolean error = false, useSecond = false, forceHttp = false;
			for (int tries = 10; tries >= 0; tries--) {
				
				for(int j=0; j<prevBufferSize; j++)
					System.out.print(' ');
				System.out.print('\r');
				String buf = "[" + (i + 1) + "/" + urls.size() + "] ";
				prevBufferSize = buf.length();
				System.out.print(buf);
				
				try {

					String target = hasSecond && useSecond && m.getImgs(true).get(i).length() > 1 ? m.getImgs(true).get(i)
							: urls.get(i);
					if(error && !useSecond){
                        target = target.indexOf("img.") > -1 ? target.replace("img.","s3.") : target.replace("://", "://s3.");
                    }
					if(forceHttp) {
						forceHttp = false;
						target = target.replace("https", "http");
					}
					
					buf = target+"\r";
					prevBufferSize += buf.length();
					System.out.print(buf);

		            URL url = new URL(target);
		            if(url.getProtocol().toLowerCase().equals("https")) {
		                HttpsURLConnection init = (HttpsURLConnection) url.openConnection();
		                int responseCode = init.getResponseCode();
		                if (responseCode >= 300 && responseCode<400) {
		                    url = new URL(init.getHeaderField("location"));
		                }else if(responseCode>=400){
		                    throw new Exception();
		                }
		            }else{
		                HttpURLConnection init = (HttpURLConnection) url.openConnection();
		                int responseCode = init.getResponseCode();
		                if (responseCode >= 300 && responseCode<400) {
		                    url = new URL(init.getHeaderField("location"));
		                }else if(responseCode>=400){
		                	throw new Exception();
		                }
		            }
		            //String fileType = url.toString().substring(url.toString().lastIndexOf('.') + 1);
		            URLConnection connection = url.openConnection();

		            String type = connection.getHeaderField("Content-Type");

		            if(!type.startsWith("image/")) {
		                //following file is not image
		            	throw new Exception();
		            }
		            
		            InputStream in = connection.getInputStream();
		            BufferedImage image = ImageIO.read(in);

					image = d.decode(image);
					int height = image.getHeight();
					int width = image.getWidth();

					if (height <= 1 || width <= 1)
						continue;

					// trim image
					if (width > height) {
						BufferedImage[] images = new BufferedImage[2];
						images[0] = image.getSubimage(width / 2, 0, width / 2, height);
						images[1] = image.getSubimage(0, 0, width / 2, height);
						// save images
						for (BufferedImage img : images) {
							File output = new File(dir, dformat.format(counter) + ".jpg");
							ImageIO.write(img, "jpg", output);
							counter++;
						}
					} else {
						// save image
						File outputFile = new File(dir, dformat.format(counter) + ".jpg");
						ImageIO.write(image, "jpg", outputFile);
						counter++;
					}
					in.close();
					break;

				}catch(SSLHandshakeException e) {
					tries--;
					forceHttp = true;
					continue;
				}catch (Exception e) {
					//e.printStackTrace();
					if (!error && !useSecond) {
						error = true;
					} else if (error && !useSecond) {
						error = false;
						useSecond = true;
					} else {
						error = false;
						useSecond = false;
					}
					continue;
				}
			}
		}
		for(int j=0; j<prevBufferSize; j++)
			System.out.print(' ');
		System.out.print('\n');
		// in case imgStepSize grounds to zero
	}


	public class SpinnerThread extends Thread {
		boolean running = false;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!isInterrupted()) {
				for (char c : spin) {
					if (running)
						System.out.print("[ " + c + " ]\r");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
				}
			}
		}

		public void pause() {
			running = false;
		}

		public void again() {
			running = true;
		}

	}

	public String getInput() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		try {
			input = br.readLine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return input;
	}

	public int getInt() {
		int i = 0;
		while (true) {
			try {
				i = Integer.parseInt(getInput());
				return i;
			} catch (Exception e) {

			}
		}
	}

	static char[] filter = { '/', '?', '*', ':', '|', '<', '>', '\\' };

	static public String filterFolder(String input) {
		for (int i = 0; i < filter.length; i++) {
			int index = input.indexOf(filter[i]);
			while (index >= 0) {
				char tmp[] = input.toCharArray();
				tmp[index] = ' ';
				input = String.valueOf(tmp);
				index = input.indexOf(filter[i]);
			}
		}
		return input;
	}

	public void commentBot() {
		setUrl();
		Login login = null;
		Boolean success = false;
		while (!success) {
			System.out.print("id: ");
			String id = getInput();
			System.out.print("password: ");
			String pass = getInput();
			login = new Login(id, pass);
			startSpin();
			success = login.submit(c);
			stopSpin();
		}

		System.out.print("사이트 확인 주기 (초): ");
		int interval = getInt();
		System.out.print("댓글 내용: ");
		String content = getInput();
		UpdatedList update = new UpdatedList();
		Manga last = null;
		printLog("startJob");
		while (true) {
			try {
				printLog("업데이트 목록 불러오기 시작");
				startSpin();
				update.firstPage();
				update.fetch(c);
				stopSpin();
				printLog("업데이트 목록 불러오기 완료");
				List<Manga> result = update.getResult();
				printLog("마지막 만화 : " + result.get(0).getName());
				if (last == null) {
					last = result.get(0);
					continue;
				}

				if (!last.equals(result.get(0))) {
					printLog("새로운 만화 발견");
					Manga prelast = null;
					for (Manga m : result) {
						if (m.equals(last)) {
							break;
						}

						printLog(m.getName() + " 에 댓글 등록");
						startSpin();
						Boolean cw = writeComment(c, login, m.getId(), content, p);
						stopSpin();
						if (cw)
							printLog(m.getName() + " 에 댓글 등록 성공");
						else
							printLog(m.getName() + " 에 댓글 등록 실패");
						prelast = m;
					}
					last = prelast;
				}
				Thread.sleep(interval * 1000);
			} catch (Exception e) {
				printLog("오류가 발생하여 종료합니다.");
				e.printStackTrace();
				return;
			}
		}

	}

	public static Boolean writeComment(CustomHttpClient client, Login login, int id, String content, Preference p) {
		try {
			Map<String, String> loginCookie = new HashMap<>();
			login.buildCookie(loginCookie);

			Response tokenResponse = client
					.getRaw(p.getUrl() + "/bbs/ajax.comment_token.php?_=" + System.currentTimeMillis(), loginCookie);
			String token = new JSONObject(tokenResponse.body().string()).getString("token");
			tokenResponse.close();
			//
			// String param = "token="+token
			// +"&w=c&bo_table=manga&wr_id="+id
			// +"&comment_id=&pim=&sca=&sfl=&stx=&spt=&page=&is_good=0&wr_content="+URLEncoder.encode(content,
			// "UTF-8");
			RequestBody requestBody = new FormBody.Builder().addEncoded("token", token).addEncoded("w", "c")
					.addEncoded("bo_table", "manga").addEncoded("wr_id", String.valueOf(id))
					.addEncoded("comment_id", "").addEncoded("pim", "").addEncoded("sca", "").addEncoded("sfl", "")
					.addEncoded("stx", "").addEncoded("spt", "").addEncoded("page", "").addEncoded("is_good", "0")
					.addEncoded("wr_content", content).build();
			Response commentResponse = client.postRaw(p.getUrl() + "/bbs/write_comment_update.php", requestBody,
					loginCookie);
			int responseCode = commentResponse.code();
			commentResponse.close();
			if (responseCode == 302)
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void printLog(String content) {
		System.out.println("[" + System.currentTimeMillis() + "]\t:" + content);
	}

}
